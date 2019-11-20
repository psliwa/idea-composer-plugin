package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.composer._
import org.psliwa.idea.composerJson.intellij.PsiElements
import PsiElements._
import org.psliwa.idea.composerJson.composer.model.{Packages, PackageName}

import scala.jdk.CollectionConverters._

private object NotInstalledPackages {
  def getNotInstalledPackageProperties(element: PsiElement, installedPackages: Packages): Seq[JsonProperty] = for {
    jsonObject <- ensureJsonObject(element).toList
    (propertyName, devPred) <- List("require" -> ((_: Boolean) == false), "require-dev" -> ((_: Boolean) => true))
    property <- findProperty(jsonObject, propertyName).toList
    packagesObject <- Option(property.getValue).toList
    packagesObject <- ensureJsonObject(packagesObject).toList
    packageProperty <- packagesObject.getPropertyList.asScala if isNotInstalled(packageProperty, devPred, installedPackages)
  } yield packageProperty

  private def isNotInstalled(property: JsonProperty, devPredicate: Boolean => Boolean, installedPackages: Packages): Boolean = {
    property.getName.contains("/") &&
      !getPackageVersion(property).isEmpty &&
      !installedPackages.get(PackageName(property.getName)).map(_.isDev).exists(devPredicate)
  }

  def getPackageVersion(property: JsonProperty): String = {
    import scala.jdk.CollectionConverters._

    val maybeVersion = for {
      value <- Option(property.getValue)
      stringLiteral <- ensureJsonStringLiteral(value)
    } yield stringLiteral.getTextFragments.asScala.foldLeft("")(_ + _.second)

    maybeVersion.getOrElse("")
  }
}
