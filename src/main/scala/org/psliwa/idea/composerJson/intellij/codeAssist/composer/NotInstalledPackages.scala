package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.composer._
import org.psliwa.idea.composerJson.intellij.PsiElements
import PsiElements._
import scala.collection.JavaConverters._

private object NotInstalledPackages {
  def getNotInstalledPackageProperties(element: PsiElement, installedPackages: ComposerPackages): Seq[JsonProperty] = for {
    jsonObject <- ensureJsonObject(element).toList
    (propertyName, devPred) <- List("require" -> pred(_ == false), "require-dev" -> pred(_ => true))
    property <- findProperty(jsonObject, propertyName).toList
    packagesObject <- Option(property.getValue).toList
    packagesObject <- ensureJsonObject(packagesObject).toList
    packageProperty <- packagesObject.getPropertyList.asScala if isNotInstalled(packageProperty, devPred, installedPackages)
  } yield packageProperty

  private def pred(f: Boolean => Boolean) = f

  private def isNotInstalled(property: JsonProperty, devPred: Boolean => Boolean, installedPackages: ComposerPackages): Boolean = {
    property.getName.contains("/") &&
      !getPackageVersion(property).isEmpty &&
      !installedPackages.get(property.getName.toLowerCase).map(_.isDev).exists(devPred)
  }

  def getPackageVersion(property: JsonProperty): String = {
    import scala.collection.JavaConverters._

    val maybeVersion = for {
      value <- Option(property.getValue)
      stringLiteral <- ensureJsonStringLiteral(value)
    } yield stringLiteral.getTextFragments.asScala.foldLeft("")(_ + _.second)

    maybeVersion.getOrElse("")
  }
}
