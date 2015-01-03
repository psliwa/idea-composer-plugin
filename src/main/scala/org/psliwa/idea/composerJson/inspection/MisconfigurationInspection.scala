package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.inspection.problem._
import org.psliwa.idea.composerJson.inspection.problem.Checker._
import org.psliwa.idea.composerJson.inspection.problem.ImplicitConversions._
import org.psliwa.idea.composerJson.json.{SString, SBoolean, Schema}

class MisconfigurationInspection extends Inspection {

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

    problemDescriptions.map(problem => problems.registerProblem(problem.element.getContext, problem.message, problem.quickFixes:_*))
  }

  private def ensureJsonObject(element: PsiElement) = element match {
    case x: JsonObject => Some(x)
    case _ => None
  }
}
