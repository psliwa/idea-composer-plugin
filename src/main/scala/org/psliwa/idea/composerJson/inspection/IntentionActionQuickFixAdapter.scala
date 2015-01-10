package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.{ProblemDescriptor, LocalQuickFix}
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class IntentionActionQuickFixAdapter(action: IntentionAction, file: PsiFile) extends LocalQuickFix {
  override def getName: String = action.getText

  override def getFamilyName: String = action.getFamilyName

  override def applyFix(project: Project, descriptor: ProblemDescriptor): Unit = {
    action.invoke(project, editorFor(project).orNull, file)
  }

  private def editorFor(project: Project): Option[Editor] = {
    Option(FileEditorManager.getInstance(project).getSelectedTextEditor)
  }
}
