package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion.{InsertionContext, InsertHandler}
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.Document
import scala.annotation.tailrec

protected[idea] case class PropertyValueInsertHandler(wrapper: String) extends InsertHandler[LookupElement] {
  assert(wrapper.length % 2 == 0)

  override def handleInsert(context: InsertionContext, item: LookupElement): Unit = {
    QuoteInsertHandler.handleInsert(context, item)

    val editor = context.getEditor
    implicit val document = editor.getDocument

    val trailingOffset = editor.getCaretModel.getOffset + 1

    document.insertString(trailingOffset, ": "+wrapper)
    val caretOffset = trailingOffset + 2 + wrapper.length / 2
    editor.getCaretModel.moveToOffset(caretOffset)

    fixTrailingComma(caretOffset + wrapper.length / 2)
  }

  private def fixTrailingComma(trailingOffset: Int)(implicit document: Document) {
    for {
      offset <- findOffset(List("\"", ",", "}"))(trailingOffset)
      _ <- ensure("\"")(offset)
    } yield {
      document.insertString(trailingOffset, ",")
    }
  }

  private def findOffset(strings: List[String])(offset: Int)(implicit doc: Document): Option[Int] = {
    val text = doc.getCharsSequence

    @tailrec
    def loop(offset: Int): Option[Int] = {
      if(offset >= text.length()) {
        None
      } else {
        val char = text.subSequence(offset, offset+1)

        if(strings.contains(char.toString)) Some(offset)
        else loop(offset + 1)
      }
    }

    loop(offset)
  }

  def ensure(s: String)(offset: Int)(implicit doc: Document) = {
    if(doc.getCharsSequence.subSequence(offset, offset+1).toString == s) Some(offset)
    else None
  }
}
