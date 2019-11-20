package org.psliwa.idea.composerJson.intellij.codeAssist.problem

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement

private[codeAssist] case class ProblemDescriptor[QuickFix](
    element: PsiElement,
    message: Option[String],
    quickFixes: Seq[QuickFix] = Seq(),
    private val maybeRange: Option[TextRange] = None,
    highlightType: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
) {
  lazy val range = maybeRange.getOrElse(element.getTextRange)
}

private[codeAssist] object ProblemDescriptor {
  def apply[QuickFix](element: PsiElement, message: String, quickFixes: Seq[QuickFix]): ProblemDescriptor[QuickFix] = {
    ProblemDescriptor(element, Some(message), quickFixes)
  }
}
