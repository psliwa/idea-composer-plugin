package org.psliwa.idea.composerJson.intellij

import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson._

import scala.annotation.tailrec

private object PsiElements {
  private val booleans = Map("true" -> true, "false" -> false)

  def ensureJsonObject(element: PsiElement): Option[JsonObject] = element match {
    case x: JsonObject => Some(x)
    case _ => None
  }

  def ensureJsonProperty(element: PsiElement): Option[JsonProperty] = element match {
    case x: JsonProperty => Some(x)
    case _ => None
  }

  def ensureJsonArray(element: PsiElement): Option[JsonArray] = element match {
    case x: JsonArray => Some(x)
    case _ => None
  }

  def ensureJsonBoolean(element: PsiElement): Option[JsonBooleanLiteral] = element match {
    case x: JsonBooleanLiteral => Some(x)
    case _ => None
  }

  def ensureJsonStringLiteral(e: PsiElement): Option[JsonStringLiteral] = e match {
    case x: JsonStringLiteral => Some(x)
    case _ => None
  }

  def ensureJsonFile(file: PsiElement): Option[JsonFile] = file match {
    case x: JsonFile => Some(x)
    case _ => None
  }

  def rootPsiElementPattern: PsiElementPattern.Capture[JsonFile] = {
    psiElement(classOf[JsonFile])
      .withLanguage(JsonLanguage.INSTANCE)
      .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
  }

  def getStringValue(value: PsiElement): Option[String] = {
    import PsiExtractors.JsonStringLiteral

    value match {
      case JsonStringLiteral(x) => Some(x)
      case _ => None
    }
  }

  def getBooleanValue(value: PsiElement): Option[Boolean] = {
    ensureJsonBoolean(value)
      .map(_.getText)
      .flatMap(booleans.get)
  }

  def findParentProperty(value: JsonElement): Option[JsonProperty] = {
    @tailrec
    def loop(element: PsiElement): Option[JsonProperty] = {
      Option(element.getParent) match {
        case Some(parent) =>
          ensureJsonProperty(parent) match {
            case Some(property) => Some(property)
            case None => loop(parent)
          }
        case None =>
          None
      }
    }

    loop(value)
  }

  def findProperty(jsonObject: JsonObject, propertyName: String): Option[JsonProperty] = {
    import scala.jdk.CollectionConverters._
    jsonObject.getPropertyList.asScala.find(_.getName == propertyName)
  }
}
