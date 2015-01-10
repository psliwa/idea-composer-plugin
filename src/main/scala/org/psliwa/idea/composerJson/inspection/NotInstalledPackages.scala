package org.psliwa.idea.composerJson.inspection

import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.composer._
import org.psliwa.idea.composerJson.util.PsiElements._
import scala.collection.JavaConversions._

private object NotInstalledPackages {
  def getNotInstalledPackageProperties(element: PsiElement, installedPackages: Packages): Seq[JsonProperty] = for {
    jsonObject <- ensureJsonObject(element).toList
    propertyName <- List("require", "require-dev")
    property <- Option(jsonObject.findProperty(propertyName)).toList
    packagesObject <- Option(property.getValue).toList
    packagesObject <- ensureJsonObject(packagesObject).toList
    packageProperty <- packagesObject.getPropertyList if isNotInstalled(packageProperty, installedPackages)
  } yield packageProperty

  private def isNotInstalled(property: JsonProperty, installedPackages: Packages): Boolean = {
    property.getName.contains("/") && !getPackageVersion(property).isEmpty && !installedPackages.contains(property.getName)
  }

  def getPackageVersion(property: JsonProperty): String = {
    import scala.collection.JavaConversions._

    val maybeVersion = for {
      value <- Option(property.getValue)
      stringLiteral <- ensureJsonStringLiteral(value)
    } yield stringLiteral.getTextFragments.foldLeft("")(_ + _.second)

    maybeVersion.getOrElse("")
  }
}
