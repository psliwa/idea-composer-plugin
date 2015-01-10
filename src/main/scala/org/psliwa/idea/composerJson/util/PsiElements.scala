package org.psliwa.idea.composerJson.util

import com.intellij.json.psi.{JsonStringLiteral, JsonFile, JsonProperty, JsonObject}
import com.intellij.psi.{PsiFile, PsiElement}

object PsiElements {
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
}
