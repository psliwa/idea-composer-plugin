package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion.{InsertionContext, InsertHandler}
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.json.JsonElementTypes
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType

protected[idea] object QuoteInsertHandler extends InsertHandler[LookupElement] {
  override def handleInsert(context: InsertionContext, item: LookupElement): Unit = {
    item.getPsiElement match {
      case LeafPsiElement(JsonElementTypes.DOUBLE_QUOTED_STRING) => //there are already quotes
      case LeafPsiElement(_) => {
        val document = context.getEditor.getDocument
        val editor = context.getEditor

        document.insertString(context.getStartOffset, "\"")
        document.insertString(context.getStartOffset + 1 + item.getLookupString.length, "\"")
        editor.getCaretModel.moveToOffset(context.getStartOffset + item.getLookupString.length + 1)
      }
      case _ =>
    }
  }
  
  private object LeafPsiElement {
    def unapply(x: LeafPsiElement): Option[(IElementType)] = Some(x.getElementType)
  }
}

