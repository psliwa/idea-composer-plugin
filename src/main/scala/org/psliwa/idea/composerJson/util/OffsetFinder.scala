package org.psliwa.idea.composerJson.util

import scala.annotation.tailrec
import scala.language.implicitConversions

case class Matcher[T](is: T => Boolean) {
  def &&(matcher: Matcher[T]) = Matcher[T](t => is(t) && matcher.is(t))
  def ||(matcher: Matcher[T]) = Matcher[T](t => is(t) || matcher.is(t))
}

trait OffsetFinder[Haystack, T] {

  object ImplicitConversions {
    implicit def objectToMatcher(o: T): Matcher[T] = Matcher(_ == o)
  }

  protected def stop(haystack: Haystack)(offset: Int): Boolean
  protected def reverseStop(haystack: Haystack)(offset: Int): Boolean
  protected def objectAt(haystack: Haystack, offset: Int): T

  def not(matcher: Matcher[T]) = Matcher[T](!matcher.is(_))

  def findOffset(matchers: Matcher[T]*)(offset: Int)(implicit haystack: Haystack): Option[Int] = {
    findOffset(Matcher[T](c => matchers.exists(_ is c)))(offset)
  }

  def findOffset(expectedMatcher: Matcher[T])(offset: Int)(implicit haystack: Haystack): Option[Int] = {
    findOffset(stop(haystack) _, 1)(expectedMatcher)(offset)
  }

  private def findOffset(stop: Int => Boolean, delta: Int)(expectedMatcher: Matcher[T])(offset: Int)(implicit text: Haystack): Option[Int] = {
    @tailrec
    def loop(offset: Int): Option[Int] = {
      if(stop(offset)) {
        None
      } else {
        val obj = objectAt(text, offset)

        if(expectedMatcher is obj) Some(offset)
        else loop(offset + delta)
      }
    }

    loop(offset)
  }

  def findOffsetReverse(expectedMatcher: Matcher[T])(offset: Int)(implicit haystack: Haystack) = {
    findOffset(reverseStop(haystack) _, -1)(expectedMatcher)(offset)
  }

  def ensure(s: Matcher[T]*)(offset: Int)(implicit haystack: Haystack) = {
    val obj = objectAt(haystack, offset)

    if(s.exists(_ is obj)) Some(obj)
    else None
  }
}
