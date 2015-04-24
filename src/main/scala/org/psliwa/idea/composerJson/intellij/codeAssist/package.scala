package org.psliwa.idea.composerJson.intellij

import com.intellij.codeInsight.completion.{CompletionParameters, InsertHandler, InsertionContext}
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson.util.CharOffsetFinder._
import org.psliwa.idea.composerJson.util.OffsetFinder.ImplicitConversions._

package object codeAssist {
  private[codeAssist] val EmptyNamePlaceholder = org.psliwa.idea.composerJson.EmptyPsiElementNamePlaceholder

  type Capture = PsiElementPattern.Capture[_ <: PsiElement]
  private[codeAssist] type InsertHandlerFinder = BaseLookupElement => Option[InsertHandler[LookupElement]]
  private[codeAssist] type LookupElements = () => Iterable[BaseLookupElement]


  private val autoPopupCondition = (context: InsertionContext) => {
    val text = context.getEditor.getDocument.getCharsSequence
    ensure('"' || ' ')(context.getEditor.getCaretModel.getOffset-1)(text).isDefined
  }

  private[codeAssist] val StringPropertyValueInsertHandler = new AutoPopupInsertHandler(Some(new PropertyValueInsertHandler("\"\"")), autoPopupCondition)
  private[codeAssist] val ObjectPropertyValueInsertHandler = new AutoPopupInsertHandler(Some(new PropertyValueInsertHandler("{}")), autoPopupCondition)
  private[codeAssist] val ArrayPropertyValueInsertHandler = new AutoPopupInsertHandler(Some(new PropertyValueInsertHandler("[]")), autoPopupCondition)
  private[codeAssist] val EmptyPropertyValueInsertHandler = new AutoPopupInsertHandler(Some(new PropertyValueInsertHandler("")), autoPopupCondition)
}
