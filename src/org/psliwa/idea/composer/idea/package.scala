package org.psliwa.idea.composer

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement

package object idea {
  protected[idea] val emptyNamePlaceholder = "IntellijIdeaRulezzz"

  protected[idea] type Capture = PsiElementPattern.Capture[_ <: PsiElement]
  protected[idea] type Keywords = () => Iterable[Keyword]

  protected[idea] type InsertHandlerFinder = Keyword => Option[InsertHandler[LookupElement]]

  protected[idea] val StringPropertyValueInsertHandler = new PropertyValueInsertHandler("\"\"")
  protected[idea] val ObjectPropertyValueInsertHandler = new PropertyValueInsertHandler("{}")
  protected[idea] val ArrayPropertyValueInsertHandler = new PropertyValueInsertHandler("[]")
  protected[idea] val EmptyPropertyValueInsertHandler = new PropertyValueInsertHandler("")
}
