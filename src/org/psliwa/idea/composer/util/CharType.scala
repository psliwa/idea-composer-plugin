package org.psliwa.idea.composer.util

import scala.annotation.tailrec
import scala.language.implicitConversions

case class CharType(is: Char => Boolean) {
  def &&(c: CharType) = CharType(char => is(char) && c.is(char))
  def ||(c: CharType) = CharType(char => is(char) || c.is(char))
}

object CharType {
  object ImplicitConversions {
    implicit def charToCharType(c: Char): CharType = CharType(_ == c)
  }

  def not(c: CharType) = CharType(!c.is(_))

  def findOffset(strings: CharType*)(offset: Int)(implicit text: CharSequence): Option[Int] = {
    findOffset(CharType(c => strings.exists(_ is c)))(offset)
  }

  def findOffset(expectedChar: CharType)(offset: Int)(implicit text: CharSequence): Option[Int] = {
    findOffset(_ >= text.length(), 1)(expectedChar)(offset)
  }

  private def findOffset(stop: Int => Boolean, delta: Int)(expectedChar: CharType)(offset: Int)(implicit text: CharSequence): Option[Int] = {
    @tailrec
    def loop(offset: Int): Option[Int] = {
      if(stop(offset)) {
        None
      } else {
        val char = text.subSequence(offset, offset+1).charAt(0)

        if(expectedChar is char) Some(offset)
        else loop(offset + delta)
      }
    }

    loop(offset)
  }

  def findOffsetReverse(expectedChar: CharType)(offset: Int)(implicit text: CharSequence) = {
    findOffset(_ < 0, -1)(expectedChar)(offset)
  }

  def ensure(s: CharType*)(offset: Int)(implicit text: CharSequence) = {
    val char = text.subSequence(offset, offset + 1).charAt(0)

    if(s.exists(_ is char)) Some(char)
    else None
  }
}
