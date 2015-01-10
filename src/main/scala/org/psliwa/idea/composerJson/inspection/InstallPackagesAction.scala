package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.psliwa.idea.composerJson.composer.command.Configuration
import org.psliwa.idea.composerJson.inspection.NotInstalledPackages._
import org.psliwa.idea.composerJson.{composer, ComposerBundle}
import org.psliwa.idea.composerJson.composer.InstalledPackages
import org.psliwa.idea.composerJson.util.PsiElements._

object InstallPackagesAction extends IntentionAction {
  override def getText: String = ComposerBundle.message("inspection.quickfix.installNotInstalledPackages")

  override def getFamilyName: String = ComposerBundle.message("inspection.group")

  override def invoke(project: Project, editor: Editor, file: PsiFile): Unit = {

    val installedPackages = InstalledPackages.forFile(file.getVirtualFile)

    val packages: Seq[composer.Package] = for {
      jsonFile <- ensureJsonFile(file).toList
      topValue <- Option(jsonFile.getTopLevelValue).toList
      pkg <- getNotInstalledPackageProperties(topValue, installedPackages).map(property => composer.Package(property.getName, getPackageVersion(property)))
    } yield pkg

    Configuration.forProject(project) match {
      case Right(config) =>
      case _ =>
    }
  }

  override def startInWriteAction(): Boolean = false

  override def isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean = true
}
