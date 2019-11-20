package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.codeInsight.completion.{InsertHandler, InsertionContext}
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.{Document, Editor}
import org.psliwa.idea.composerJson.util.CharOffsetFinder._
import org.psliwa.idea.composerJson.util.Matcher
import org.psliwa.idea.composerJson.util.OffsetFinder.ImplicitConversions._

import scala.language.implicitConversions

private[codeAssist] case class PropertyValueInsertHandler(wrapper: String) extends InsertHandler[LookupElement] {
  assert(wrapper.length % 2 == 0)

  lazy val wrappingOpenChar: Option[Char] = wrapper.headOption

  override def handleInsert(context: InsertionContext, item: LookupElement): Unit = {
    QuoteInsertHandler.handleInsert(context, item)

    implicit val editor: Editor = context.getEditor
    implicit val document: Document = editor.getDocument
    implicit val text: CharSequence = document.getCharsSequence

    val trailingOffset = editor.getCaretModel.getOffset + 1

    findExistingValueRange(trailingOffset) match {
      case Some(range) => selectRange(range)
      case None => completeValue(trailingOffset)
    }
  }

  import PropertyValueInsertHandler._

  private def findExistingValueRange(offset: Int)(implicit text: CharSequence): Option[Range] = {
    for {
      colonOffset <- findOffset(OpenControlChar || ':')(offset)
      _ <- ensure(':')(colonOffset)
      headOffset <- findOffset(not(Whitespace))(colonOffset + 1)
      openChar <- ensure(OpenControlChar || Alphnum || '.')(headOffset)
      openChar <- replaceCharIf(openChar, is = Alphnum || '.', replacement = ' ')
      (closeCharType, delta) <- oppositeChar(openChar)
      trailingOffset <- findCloseCharOffset(openChar, closeCharType)(headOffset + delta)
      closeChar <- ensure(closeCharType)(trailingOffset)
    } yield {
      Range(
        headOffset,
        trailingOffset + delta,
        Some(openChar).filter(OpenControlChar.is),
        Some(closeChar).filter(CloseControlChar.is)
      )
    }
  }

  private def oppositeChar(s: Char): Option[(CharMatcher, Int)] = {
    val map = Map[Char, CharMatcher]('"' -> '"', '{' -> '}', '[' -> ']').view.mapValues((_, 1))
    map.get(s).orElse(Some((not(Alphnum || '.'), 0)))
  }

  private def fixTrailingComma(trailingOffset: Int)(implicit document: Document) {
    for {
      offset <- findOffset('"', ',', '}')(trailingOffset)(document.getCharsSequence)
      _ <- ensure('"')(offset)(document.getCharsSequence)
    } yield {
      document.insertString(trailingOffset, ",")
    }
  }

  private def findCloseCharOffset(openChar: CharMatcher,
                                  closeChar: CharMatcher)(offset: Int)(implicit text: CharSequence): Option[Int] = {
    @scala.annotation.tailrec
    def loop(offset: Int, deep: Int): Option[Int] = {
      val foundOffset = findOffset(openChar || closeChar)(offset)
      val success = foundOffset.map(ensure(closeChar)(_).isDefined)

      success match {
        case None => None
        case Some(true) if deep > 0 => loop(foundOffset.get + 1, deep - 1)
        case Some(true) => foundOffset
        case Some(false) => loop(foundOffset.get + 1, deep + 1)
      }
    }

    loop(offset, 0)
  }

  private def replaceCharIf(char: Char, is: CharMatcher, replacement: Char): Option[Char] = {
    if (is is char) Some(replacement)
    else Some(char)
  }

  private def selectRange(range: Range)(implicit editor: Editor) {
    val fixedRange = range.eatWrapperIf(wrapper != range.wrapper)
    editor.getCaretModel.moveToOffset(fixedRange.headOffset)
    editor.getCaretModel.getPrimaryCaret.setSelection(fixedRange.headOffset, fixedRange.trailingOffset)
  }

  private def completeValue(offset: Int)(implicit editor: Editor) {
    implicit val document: Document = editor.getDocument

    document.insertString(offset, ": " + wrapper)
    val caretOffset = offset + 2 + wrapper.length / 2
    editor.getCaretModel.moveToOffset(caretOffset)

    fixTrailingComma(caretOffset + wrapper.length / 2)
  }
}

private object PropertyValueInsertHandler {
  case class Range(private val ho: Int, private val to: Int, openChar: Option[Char], closeChar: Option[Char]) {
    val headOffset: Int = ho + openChar.map(_ => 1).getOrElse(0)
    val trailingOffset: Int = to - closeChar.map(_ => 1).getOrElse(0)
    val wrapper: String = openChar.map(_.toString).getOrElse("") + closeChar.map(_.toString).getOrElse("")

    def eatWrapper = Range(ho, to, None, None)
    def eatWrapperIf(b: Boolean): Range = if (b) eatWrapper else this
  }

  val OpenControlChar: Matcher[Char] = '"' || '{' || '['
  val CloseControlChar: Matcher[Char] = '"' || '}' || ']'
}
