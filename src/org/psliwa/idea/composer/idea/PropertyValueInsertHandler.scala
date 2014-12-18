package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion.{InsertionContext, InsertHandler}
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.{Editor, Document}
import scala.annotation.tailrec
import scala.language.implicitConversions

protected[idea] case class PropertyValueInsertHandler(wrapper: String) extends InsertHandler[LookupElement] {
  assert(wrapper.length % 2 == 0)

  lazy val wrappingOpenChar = wrapper.headOption
  
  import PropertyValueInsertHandler._

  override def handleInsert(context: InsertionContext, item: LookupElement): Unit = {
    QuoteInsertHandler.handleInsert(context, item)

    implicit val editor = context.getEditor
    implicit val document = editor.getDocument

    val trailingOffset = editor.getCaretModel.getOffset + 1

    findExistingValueRange(trailingOffset) match {
      case Some(range) => selectRange(range)
      case None => completeValue(trailingOffset)
    }
  }

  private def findExistingValueRange(offset: Int)(implicit document: Document): Option[Range] = {
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

  private def oppositeChar(s: Char): Option[(CharType,Int)] = {
    val map = Map[Char,CharType]('"' -> '"', '{' -> '}', '[' -> ']').mapValues((_,1))
    map.get(s).orElse(Some((not(Alphnum || '.'),0)))
  }

  private def fixTrailingComma(trailingOffset: Int)(implicit document: Document) {
    for {
      offset <- findOffset('"', ',', '}')(trailingOffset)
      _ <- ensure('"')(offset)
    } yield {
      document.insertString(trailingOffset, ",")
    }
  }

  private def findOffset(strings: CharType*)(offset: Int)(implicit doc: Document): Option[Int] = {
    findOffset(CharType(c => strings.exists(_ is c)))(offset)
  }

  private def findCloseCharOffset(openChar: CharType, closeChar: CharType)(offset: Int)(implicit doc: Document): Option[Int] = {
    def loop(offset: Int, deep: Int): Option[Int] = {
      val foundOffset = findOffset(openChar || closeChar)(offset)
      val success = foundOffset.map(ensure(closeChar)(_).isDefined)

      success match {
        case None => None
        case Some(true) if deep > 0 => loop(foundOffset.get+1, deep - 1)
        case Some(true) => foundOffset
        case Some(false) => loop(foundOffset.get+1, deep + 1)
      }
    }

    loop(offset, 0)
  }

  private def findOffset(expectedChar: CharType)(offset: Int)(implicit doc: Document): Option[Int] = {
    val text = doc.getCharsSequence

    @tailrec
    def loop(offset: Int): Option[Int] = {
      if(offset >= text.length()) {
        None
      } else {
        val char = text.subSequence(offset, offset+1).charAt(0)

        if(expectedChar is char) Some(offset)
        else loop(offset + 1)
      }
    }

    loop(offset)
  }

  private def ensure(s: CharType*)(offset: Int)(implicit doc: Document) = {
    val char = doc.getCharsSequence.subSequence(offset, offset + 1).charAt(0)

    if(s.exists(_ is char)) Some(char)
    else None
  }

  private def replaceCharIf(char: Char, is: CharType, replacement: Char): Option[Char] = {
    if(is is char) Some(replacement)
    else Some(char)
  }

  private def selectRange(range: Range)(implicit editor: Editor) {
    val fixedRange = range.eatWrapperIf(wrapper != range.wrapper)
    editor.getCaretModel.moveToOffset(fixedRange.headOffset)
    editor.getCaretModel.getPrimaryCaret.setSelection(fixedRange.headOffset, fixedRange.trailingOffset)
  }

  private def completeValue(offset: Int)(implicit editor: Editor) {
    implicit val document = editor.getDocument

    document.insertString(offset, ": " + wrapper)
    val caretOffset = offset + 2 + wrapper.length / 2
    editor.getCaretModel.moveToOffset(caretOffset)

    fixTrailingComma(caretOffset + wrapper.length / 2)
  }
}

private object PropertyValueInsertHandler {
  case class Range(private val ho: Int, private val to: Int, openChar: Option[Char], closeChar: Option[Char]) {
    val headOffset = ho + openChar.map(_ => 1).getOrElse(0)
    val trailingOffset = to - closeChar.map(_ => 1).getOrElse(0)
    val wrapper = openChar.map(_.toString).getOrElse("") + closeChar.map(_.toString).getOrElse("")

    def eatWrapper = Range(ho, to, None, None)
    def eatWrapperIf(b: Boolean) = if(b) eatWrapper else this
  }
  case class CharType(is: Char => Boolean) {
    def &&(c: CharType) = CharType(char => is(char) && c.is(char))
    def ||(c: CharType) = CharType(char => is(char) || c.is(char))
  }
  implicit def charToCharType(c: Char): CharType = CharType(_ == c)
  def not(c: CharType) = CharType(!c.is(_))

  val Whitespace = CharType(_.isWhitespace)
  val Alphnum = CharType(_.isLetterOrDigit)
  val OpenControlChar = '"' || '{' || '['
  val CloseControlChar = '"' || '}' || ']'
}
