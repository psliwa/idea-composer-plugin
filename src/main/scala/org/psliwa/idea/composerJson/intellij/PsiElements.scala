package org.psliwa.idea.composerJson.intellij

import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.{PsiElement, PsiFile}
import org.psliwa.idea.composerJson._

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


  def ensureJsonFile(file: PsiElement) = file match {
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
}
