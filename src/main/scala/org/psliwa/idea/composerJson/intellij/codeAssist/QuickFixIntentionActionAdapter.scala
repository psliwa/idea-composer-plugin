package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Comparing
import com.intellij.psi.PsiFile

private class QuickFixIntentionActionAdapter(quickFix: LocalQuickFixOnPsiElement) extends IntentionAction {
  override def getText: String = quickFix.getName
  override def getFamilyName: String = quickFix.getFamilyName
  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = quickFix.applyFix()
  override def startInWriteAction(): Boolean = true
  override def isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean = {
    quickFix.getStartElement != null && quickFix.isAvailable(
      project,
      file,
      quickFix.getStartElement,
      if (quickFix.getEndElement == null) quickFix.getStartElement else quickFix.getEndElement
    )
  }
}

private class QuickFixIntentionActionAdapterWithPriority(quickFix: LocalQuickFixOnPsiElement, private val priority: Int)
    extends QuickFixIntentionActionAdapter(quickFix)
    with Comparable[IntentionAction] {
  override def compareTo(o: IntentionAction): Int = {
    o match {
      case p: QuickFixIntentionActionAdapterWithPriority =>
        val diff = p.priority - priority
        if (diff == 0) {
          Comparing.compare(getText, o.getText)
        } else {
          p.priority - priority
        }
      case _ =>
        Comparing.compare(getText, o.getText)
    }
  }
}
