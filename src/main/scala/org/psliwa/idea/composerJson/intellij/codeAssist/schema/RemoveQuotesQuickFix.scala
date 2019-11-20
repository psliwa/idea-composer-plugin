package org.psliwa.idea.composerJson.intellij.codeAssist.schema

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiFile}
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.intellij.PsiElements
import org.psliwa.idea.composerJson.intellij.codeAssist.QuickFix
import QuickFix._
import org.psliwa.idea.composerJson.intellij.codeAssist.QuickFix
import PsiElements._

private class RemoveQuotesQuickFix(element: PsiElement) extends LocalQuickFixOnPsiElement(element) {

  override def invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
    for {
      stringLiteral <- ensureJsonStringLiteral(element)
      document <- documentFor(project, file)
    } yield {
      val headOffset = getHeadOffset(stringLiteral)
      val trailingOffset = headOffset + stringLiteral.getText.length - 2

      document.replaceString(headOffset, headOffset + 1, "")
      document.replaceString(trailingOffset, trailingOffset + 1, "")
    }
  }

  override def getText: String = ComposerBundle.message("inspection.quickfix.removeQuotes")
  override def getFamilyName: String = ComposerBundle.message("inspection.group")
}
