package org.psliwa.idea.composerJson.intellij.codeAssist.composer.infoRenderer

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.composer.InstalledPackages
import org.psliwa.idea.composerJson.intellij.PsiElements
import org.psliwa.idea.composerJson.intellij.codeAssist.AbstractInspection
import org.psliwa.idea.composerJson.json.Schema
import PsiElements._
import org.psliwa.idea.composerJson.composer.model.{PackageDescriptor, PackageName}

import scala.jdk.CollectionConverters._

class PackageInfoInspection extends AbstractInspection {
  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    val installedPackages = InstalledPackages.forFile(element.getContainingFile.getVirtualFile)

    def packageInfo(pkg: PackageDescriptor): String = {
      pkg.replacedBy.map(p => s"replaced by: ${p.name.presentation} (${p.version})").getOrElse(pkg.version)
    }

    val packagesInfo = for {
      jsonObject <- ensureJsonObject(element).toList
      propertyName <- List("require", "require-dev")
      property <- findProperty(jsonObject, propertyName).toList
      packagesObject <- Option(property.getValue).toList
      packagesObject <- ensureJsonObject(packagesObject).toList
      packageProperty <- packagesObject.getPropertyList.asScala
      pkg <- installedPackages.get(PackageName(packageProperty.getName)).toList
    } yield {
      PackageInfo(packageProperty.getTextOffset, packageInfo(pkg))
    }

    Option(ApplicationManager.getApplication.getComponent(classOf[PackageInfoOverlay]))
      .foreach(_.setPackagesInfo(element.getContainingFile.getVirtualFile.getCanonicalPath, packagesInfo))
  }
}
