package org.psliwa.idea.composer

import com.intellij.codeInsight.completion.{InsertionContext, InsertHandler}
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import org.psliwa.idea.composer.util.CharType._
import org.psliwa.idea.composer.util.CharType.ImplicitConversions._

package object idea {
  protected[idea] val emptyNamePlaceholder = "IntellijIdeaRulezzz"

  protected[idea] type Capture = PsiElementPattern.Capture[_ <: PsiElement]
  protected[idea] type LookupElements = () => Iterable[BaseLookupElement]

  protected[idea] type InsertHandlerFinder = BaseLookupElement => Option[InsertHandler[LookupElement]]

  private val autoPopupCondition = (context: InsertionContext) => {
    val text = context.getEditor.getDocument.getCharsSequence
    ensure('"' || ' ')(context.getEditor.getCaretModel.getOffset-1)(text).isDefined
  }

  protected[idea] val StringPropertyValueInsertHandler = new AutoPopupInsertHandler(Some(new PropertyValueInsertHandler("\"\"")), autoPopupCondition)
  protected[idea] val ObjectPropertyValueInsertHandler = new AutoPopupInsertHandler(Some(new PropertyValueInsertHandler("{}")), autoPopupCondition)
  protected[idea] val ArrayPropertyValueInsertHandler = new AutoPopupInsertHandler(Some(new PropertyValueInsertHandler("[]")), autoPopupCondition)
  protected[idea] val EmptyPropertyValueInsertHandler = new AutoPopupInsertHandler(Some(new PropertyValueInsertHandler("")), autoPopupCondition)
}
