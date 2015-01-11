package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.psliwa.idea.composerJson.composer.InstalledPackages
import org.psliwa.idea.composerJson.composer.command.{Configuration, DefaultPackagesInstaller}
import org.psliwa.idea.composerJson.inspection.NotInstalledPackages._
import org.psliwa.idea.composerJson.util.PsiElements._
import org.psliwa.idea.composerJson.{ComposerBundle, composer}

object InstallPackagesAction extends IntentionAction {

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
      pkg <- getNotInstalledPackageProperties(topValue, installedPackages).map(property => composer.Package(property.getName, getPackageVersion(property)))
    } yield pkg

    if(packages.size > 0) {
      Configuration.forProject(project) match {
        case Right(config) => new DefaultPackagesInstaller(project, config, file).install(packages)
        case _ =>
      }
    }
  }

  override def startInWriteAction(): Boolean = false

  override def isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean = true
}
