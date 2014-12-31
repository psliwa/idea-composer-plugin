package org.psliwa.idea.composerJson.util

import scala.language.implicitConversions

object CharType extends OffsetFinder[CharSequence, Char] {

  type CharType = Matcher[Char]

  protected def objectAt(haystack: CharSequence, offset: Int): Char = {
    haystack.subSequence(offset, offset + 1).charAt(0)
  }

  protected def stop(haystack: CharSequence)(offset: Int): Boolean = offset >= haystack.length()
  protected def reverseStop(haystack: CharSequence)(offset: Int): Boolean = offset < 0
}
