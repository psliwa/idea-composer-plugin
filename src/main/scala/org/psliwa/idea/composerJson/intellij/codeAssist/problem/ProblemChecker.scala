package org.psliwa.idea.composerJson.intellij.codeAssist.problem

import com.intellij.codeInspection.{LocalQuickFixOnPsiElement, ProblemHighlightType}
import com.intellij.json.psi.JsonObject
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.checker.Checker

import scala.language.implicitConversions

private[codeAssist] case class ProblemChecker(
    checker: Checker,
    problem: String,
    createQuickFixes: (JsonObject, PropertyPath) => List[LocalQuickFixOnPsiElement] = (_, _) => List.empty,
    highlightType: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
    elements: JsonObject => List[JsonObject] = List(_: JsonObject)
) extends Checker {
  override def check(jsonObject: JsonObject): CheckResult = checker.check(jsonObject)
}
