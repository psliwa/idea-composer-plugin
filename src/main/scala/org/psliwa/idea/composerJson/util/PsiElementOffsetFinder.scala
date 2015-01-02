package org.psliwa.idea.composerJson.util

import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiElement

import scala.language.implicitConversions

object PsiElementOffsetFinder extends OffsetFinder[JsonObject,PsiElement] {
  override protected def stop(haystack: JsonObject)(offset: Int): Boolean = !haystack.getTextRange.containsOffset(offset)
  override def objectAt(haystack: JsonObject, offset: Int): PsiElement = {
    haystack.getChildren
      .find(_.getTextRange.contains(offset))
      .getOrElse(haystack.findElementAt(offset))
  }
  override protected def reverseStop(haystack: JsonObject)(offset: Int): Boolean = stop(haystack)(offset)

  object ImplicitConversions {
    implicit def typeToMatcher[A <: PsiElement](cls: Class[A]): Matcher[PsiElement] = Matcher(t => cls.isAssignableFrom(t.getClass))
  }
}
