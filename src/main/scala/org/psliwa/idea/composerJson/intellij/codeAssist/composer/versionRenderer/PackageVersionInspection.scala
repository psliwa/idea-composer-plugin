package org.psliwa.idea.composerJson.intellij.codeAssist.composer.versionRenderer

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.composer.InstalledPackages
import org.psliwa.idea.composerJson.intellij.PsiElements
import org.psliwa.idea.composerJson.intellij.codeAssist.AbstractInspection
import org.psliwa.idea.composerJson.json.Schema
import PsiElements._
import scala.collection.JavaConversions._

class PackageVersionInspection extends AbstractInspection {
  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    val installedPackages = InstalledPackages.forFile(element.getContainingFile.getVirtualFile)

    val packageVersions = for {
      jsonObject <- ensureJsonObject(element).toList
      propertyName <- List("require", "require-dev")
      property <- Option(jsonObject.findProperty(propertyName)).toList
      packagesObject <- Option(property.getValue).toList
      packagesObject <- ensureJsonObject(packagesObject).toList
      packageProperty <- packagesObject.getPropertyList
      pkg <- installedPackages.get(packageProperty.getName).toList
    } yield {
      PackageVersion(packageProperty.getTextOffset, pkg.version)
    }

    Option(ApplicationManager.getApplication.getComponent(classOf[VersionOverlay]))
      .foreach(_.setPackageVersions(element.getContainingFile.getVirtualFile.getCanonicalPath, packageVersions.toList))
  }
}
