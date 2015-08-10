package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.codeInspection.{ProblemHighlightType, ProblemsHolder}
import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.intellij.PsiElements
import org.psliwa.idea.composerJson.intellij.codeAssist.{CreatePropertyQuickFix, AbstractInspection, SetPropertyValueQuickFix}
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.Checker._
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.ImplicitConversions._
import org.psliwa.idea.composerJson.intellij.codeAssist.problem._
import org.psliwa.idea.composerJson.json.{SBoolean, SString, Schema}
import org.psliwa.idea.composerJson.intellij.PsiElements.findProperty
import PsiElements._

class MisconfigurationInspection extends AbstractInspection {

  private val problemCheckers = List(
    ProblemChecker(
      ("type" is "project") && ("minimum-stability" isNot "stable") && (("prefer-stable" is false) || not("prefer-stable")),
      List("type", "minimum-stability", "prefer-stable"),
      ComposerBundle.message("inspection.misconfig.notStableProject"),
      (jsonObject) => List(
        new SetPropertyValueQuickFix(jsonObject, "prefer-stable", SBoolean, "true"),
        new SetPropertyValueQuickFix(jsonObject, "minimum-stability", SString(), "stable")
      )
    ),
    requiredPropertyForLibrary("name"),
    requiredPropertyForLibrary("description")
  )

  private def requiredPropertyForLibrary(property: String) = ProblemChecker(
    ("type" isNot "project") && not(property),
    List("type"),
    ComposerBundle.message("inspection.misconfig.requiredForLibrary", property),
    (jsonObject) => List(
      new CreatePropertyQuickFix(jsonObject, property, new SString()),
      new SetPropertyValueQuickFix(jsonObject, "type", new SString(), "project")
    ),
    ProblemHighlightType.GENERIC_ERROR
  )

  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    ensureJsonObject(element)
      .foreach(collectProblems(_, problems))
  }

  private def collectProblems(jsonObject: JsonObject, problems: ProblemsHolder): Unit = {
    val problemDescriptions = problemCheckers.flatMap(checker => {
      if(checker.check(jsonObject)) {
        checker.properties
          .flatMap(findProperty(jsonObject, _))
          .flatMap(value => Option(value.getValue))
          .map(value => ProblemDescriptor(
            element = value,
            message = Some(checker.problem),
            quickFixes = checker.createQuickFixes(jsonObject),
            highlightType = checker.highlightType
          ))
      } else {
        List()
      }
    })

    problemDescriptions.foreach(problem =>
      problems.registerProblem(problem.element.getContext, problem.message.getOrElse(""), problem.highlightType, problem.quickFixes:_*)
    )
  }
}
