package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion.{InsertionContext, InsertHandler}
import com.intellij.codeInsight.lookup.LookupElement
import org.psliwa.idea.composer.util.CharType._
import org.psliwa.idea.composer.util.CharType.ImplicitConversions._

protected[idea] object QuoteInsertHandler extends InsertHandler[LookupElement] {
  override def handleInsert(context: InsertionContext, item: LookupElement): Unit = {
    val document = context.getEditor.getDocument
    val editor = context.getEditor

    implicit val text = document.getCharsSequence

    for {
      offset <- findOffsetReverse('"' || ':' || '{' || '}' || '[' || ']')(context.getStartOffset)
      _ <- ensure(not('"'))(offset)
    } yield {
      document.insertString(context.getStartOffset, "\"")
      document.insertString(context.getStartOffset + 1 + item.getLookupString.length, "\"")
      editor.getCaretModel.moveToOffset(context.getStartOffset + item.getLookupString.length + 1)
    }
  }
}

