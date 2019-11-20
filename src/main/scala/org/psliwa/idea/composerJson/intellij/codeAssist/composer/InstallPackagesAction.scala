package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.composer._
import org.psliwa.idea.composerJson.composer.command.DefaultPackagesInstaller
import org.psliwa.idea.composerJson.composer.model.PackageName
import org.psliwa.idea.composerJson.intellij.PsiElements._
import org.psliwa.idea.composerJson.intellij.codeAssist.composer.NotInstalledPackages._

private object InstallPackagesAction extends IntentionAction {

  override def getText: String = ComposerBundle.message("inspection.quickfix.installNotInstalledPackages")
  override def getFamilyName: String = ComposerBundle.message("inspection.group")

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {

    val documentManager = FileDocumentManager.getInstance()

    for {
      document <- Option(documentManager.getDocument(file.getVirtualFile))
    } yield documentManager.saveDocument(document)

    val installedPackages = InstalledPackages.forFile(file.getVirtualFile)

    val packages = for {
      jsonFile <- ensureJsonFile(file).toList
      topValue <- Option(jsonFile.getTopLevelValue).toList
      packageName <- getNotInstalledPackageProperties(topValue, installedPackages).map(
                      property => PackageName(property.getName)
                    )
    } yield packageName

    if (packages.nonEmpty) {
      new DefaultPackagesInstaller(project, file).install(packages)
    }
  }

  override def startInWriteAction(): Boolean = false

  override def isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean = true
}
