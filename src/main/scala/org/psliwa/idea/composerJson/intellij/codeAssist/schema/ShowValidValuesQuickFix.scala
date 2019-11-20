package org.psliwa.idea.composerJson.intellij.codeAssist.schema

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiElement, PsiFile}
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.intellij.codeAssist.QuickFix
import QuickFix._
import org.psliwa.idea.composerJson.intellij.codeAssist.QuickFix

private class ShowValidValuesQuickFix(element: JsonStringLiteral) extends LocalQuickFixOnPsiElement(element) {
  override def getText: String = ComposerBundle.message("inspection.quickfix.chooseValidValue")

  override def invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
    for {
      _ <- documentFor(project, file)
      editor <- editorFor(project)
    } yield {
      val range = element.getTextRange
      editor.getCaretModel.getPrimaryCaret.setSelection(range.getStartOffset + 1, range.getEndOffset - 1)
      AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
    }
  }

  override def getFamilyName: String = ComposerBundle.message("inspection.group")
}
