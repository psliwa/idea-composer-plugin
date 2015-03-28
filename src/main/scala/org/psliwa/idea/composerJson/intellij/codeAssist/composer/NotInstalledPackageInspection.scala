package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.composer.InstalledPackages
import org.psliwa.idea.composerJson.intellij.codeAssist.{AbstractInspection, IntentionActionQuickFixAdapter}
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.ProblemDescriptor
import org.psliwa.idea.composerJson.json.Schema

class NotInstalledPackageInspection extends AbstractInspection {

  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    val installedPackages = InstalledPackages.forFile(element.getContainingFile.getVirtualFile)
    val notInstalledPackageProperties = NotInstalledPackages.getNotInstalledPackageProperties(element, installedPackages)

    notInstalledPackageProperties
      .map(createProblem)
      .foreach(problem => problems.registerProblem(problem.element, problem.message.getOrElse(""), problem.quickFixes:_*))
  }

  private def createProblem(property: JsonProperty) = {
    ProblemDescriptor(
      property,
      ComposerBundle.message("inspection.notIninstalledPackage.packageIsNotInstalled", property.getName),
      Seq(new IntentionActionQuickFixAdapter(InstallPackagesAction, property.getContainingFile))
    )
  }
}
