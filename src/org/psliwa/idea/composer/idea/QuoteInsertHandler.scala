package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion.{InsertionContext, InsertHandler}
import com.intellij.codeInsight.lookup.LookupElement

protected[idea] object QuoteInsertHandler extends InsertHandler[LookupElement] {
  override def handleInsert(context: InsertionContext, item: LookupElement): Unit = {
    val document = context.getEditor.getDocument
    val editor = context.getEditor

    document.insertString(context.getStartOffset, "\"")
    document.insertString(context.getStartOffset + 1 + item.getLookupString.length, "\"")

    editor.getCaretModel.moveToOffset(context.getStartOffset + item.getLookupString.length + 2)
  }
}