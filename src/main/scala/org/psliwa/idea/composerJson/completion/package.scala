package org.psliwa.idea.composerJson

import com.intellij.codeInsight.completion.{InsertionContext, InsertHandler}
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.util.CharOffsetFinder._
import org.psliwa.idea.composerJson.util.OffsetFinder.ImplicitConversions._

package object completion {
  protected[completion] val EmptyNamePlaceholder = org.psliwa.idea.composerJson.EmptyPsiElementNamePlaceholder

  protected[completion] type Capture = PsiElementPattern.Capture[_ <: PsiElement]
  protected[completion] type LookupElements = () => Iterable[BaseLookupElement]

  protected[completion] type InsertHandlerFinder = BaseLookupElement => Option[InsertHandler[LookupElement]]

  private val autoPopupCondition = (context: InsertionContext) => {
    val text = context.getEditor.getDocument.getCharsSequence
    ensure('"' || ' ')(context.getEditor.getCaretModel.getOffset-1)(text).isDefined
  }

  protected[completion] val StringPropertyValueInsertHandler = new AutoPopupInsertHandler(Some(new PropertyValueInsertHandler("\"\"")), autoPopupCondition)
  protected[completion] val ObjectPropertyValueInsertHandler = new AutoPopupInsertHandler(Some(new PropertyValueInsertHandler("{}")), autoPopupCondition)
  protected[completion] val ArrayPropertyValueInsertHandler = new AutoPopupInsertHandler(Some(new PropertyValueInsertHandler("[]")), autoPopupCondition)
  protected[completion] val EmptyPropertyValueInsertHandler = new AutoPopupInsertHandler(Some(new PropertyValueInsertHandler("")), autoPopupCondition)
}
