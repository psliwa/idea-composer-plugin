package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

private class QuickFixIntentionActionAdapter(quickFix: LocalQuickFixOnPsiElement) extends IntentionAction {
  override def getText: String = quickFix.getName
  override def getFamilyName: String = quickFix.getFamilyName
  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = quickFix.applyFix()
  override def startInWriteAction(): Boolean = true
  override def isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean = {
    quickFix.isAvailable(
      project,
      file,
      quickFix.getStartElement,
      if(quickFix.getEndElement == null) quickFix.getStartElement else quickFix.getEndElement
    )
  }
}
