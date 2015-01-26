package org.psliwa.idea.composerJson.intellij

import com.intellij.json.JsonLanguage
import com.intellij.json.psi.{JsonFile, JsonObject, JsonProperty, JsonStringLiteral}
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.{PsiElement, PsiFile}
import org.psliwa.idea.composerJson._

private object PsiElements {
  def ensureJsonObject(element: PsiElement): Option[JsonObject] = element match {
    case x: JsonObject => Some(x)
    case _ => None
  }

  def ensureJsonProperty(element: PsiElement): Option[JsonProperty] = element match {
    case x: JsonProperty => Some(x)
    case _ => None
  }

  def ensureJsonStringLiteral(e: PsiElement): Option[JsonStringLiteral] = e match {
    case x: JsonStringLiteral => Some(x)
    case _ => None
  }


  def ensureJsonFile(file: PsiFile) = file match {
    case x: JsonFile => Some(x)
    case _ => None
  }

  def rootPsiElementPattern: PsiElementPattern.Capture[JsonFile] = {
    psiElement(classOf[JsonFile])
      .withLanguage(JsonLanguage.INSTANCE)
      .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
  }
}
