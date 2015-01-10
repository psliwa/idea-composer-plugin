package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiFile, PsiElement}
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.inspection.QuickFix._
import org.psliwa.idea.composerJson.util.PsiElements._

private[inspection] class RemoveQuotesQuickFix(element: PsiElement) extends LocalQuickFixOnPsiElement(element){

  override def invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
    for {
      stringLiteral <- ensureJsonStringLiteral(element)
      document <- documentFor(project, file)
    } yield {
      val headOffset = getHeadOffset(stringLiteral)
      val trailingOffset = headOffset + stringLiteral.getText.length - 2

      document.replaceString(headOffset, headOffset+1, "")
      document.replaceString(trailingOffset, trailingOffset+1, "")
    }
  }

  override def getText: String = ComposerBundle.message("inspection.quickfix.removeQuotes")
  override def getFamilyName: String = ComposerBundle.message("inspection.group")
}
