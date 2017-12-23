package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.settings.{ProjectSettings, PatternItem}

private class ExcludePatternAction(pattern: String) extends IntentionAction {
  override def getText: String = ComposerBundle.message("inspection.quickfix.excludePackagePattern", pattern)

  private def settings(project: Project) = {
    ProjectSettings(project)
  }

  override def getFamilyName: String = ComposerBundle.message("inspection.group")

  override def isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean = true

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {
    settings(project).getUnboundedVersionInspectionSettings.addExcludedPattern(new PatternItem(pattern))
    //force reanalyse file
    DaemonCodeAnalyzer.getInstance(project).restart(file)
  }

  override def startInWriteAction(): Boolean = true
}
