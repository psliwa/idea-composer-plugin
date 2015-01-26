package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.intellij.PsiElements
import org.psliwa.idea.composerJson.intellij.codeAssist.{AbstractInspection, SetPropertyValueQuickFix}
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.Checker._
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.ImplicitConversions._
import org.psliwa.idea.composerJson.intellij.codeAssist.problem._
import org.psliwa.idea.composerJson.json.{SBoolean, SString, Schema}
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
    )
  )

  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    ensureJsonObject(element)
      .foreach(collectProblems(_, problems))
  }

  private def collectProblems(jsonObject: JsonObject, problems: ProblemsHolder): Unit = {
    val problemDescriptions = problemCheckers.flatMap(checker => {
      if(checker.check(jsonObject)) {
        checker.properties
          .map(name => Option(jsonObject.findProperty(name)))
          .filter(_ != None)
          .flatMap(value => Option(value.get.getValue))
          .map(value => ProblemDescriptor(value, checker.problem, checker.createQuickFixes(jsonObject)))
      } else {
        List()
      }
    })

    problemDescriptions.foreach(problem => problems.registerProblem(problem.element.getContext, problem.message, problem.quickFixes:_*))
  }
}
