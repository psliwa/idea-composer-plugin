package org.psliwa.idea.composerJson.util

import scala.annotation.tailrec
import scala.language.implicitConversions

case class Matcher[A](is: A => Boolean) {
  def &&(matcher: Matcher[A]) = Matcher[A](t => is(t) && matcher.is(t))
  def ||(matcher: Matcher[A]) = Matcher[A](t => is(t) || matcher.is(t))
}

trait OffsetFinder[Haystack, A] {

  protected def stop(haystack: Haystack)(offset: Int): Boolean
  protected def reverseStop(haystack: Haystack)(offset: Int): Boolean
  protected def objectAt(haystack: Haystack, offset: Int): A

  def not(matcher: Matcher[A]) = Matcher[A](!matcher.is(_))

  def findOffset(matchers: Matcher[A]*)(offset: Int)(implicit haystack: Haystack): Option[Int] = {
    findOffset(Matcher[A](c => matchers.exists(_ is c)))(offset)
  }

  def findOffset(expectedMatcher: Matcher[A])(offset: Int)(implicit haystack: Haystack): Option[Int] = {
    findOffset(stop(haystack) _, 1)(expectedMatcher)(offset)
  }

  private def findOffset(stop: Int => Boolean, delta: Int)(
      expectedMatcher: Matcher[A]
  )(offset: Int)(implicit haystack: Haystack): Option[Int] = {
    @tailrec
    def loop(offset: Int): Option[Int] = {
      if (stop(offset)) {
        None
      } else {
        val obj = objectAt(haystack, offset)

        if (expectedMatcher is obj) Some(offset)
        else loop(offset + delta)
      }
    }

    loop(offset)
  }

  def findOffsetReverse(expectedMatcher: Matcher[A])(offset: Int)(implicit haystack: Haystack) = {
    findOffset(reverseStop(haystack) _, -1)(expectedMatcher)(offset)
  }

  def ensure(s: Matcher[A]*)(offset: Int)(implicit haystack: Haystack) = {
    val obj = objectAt(haystack, offset)

    if (s.exists(_ is obj)) Some(obj)
    else None
  }
}

object OffsetFinder {
  object ImplicitConversions {
    implicit def objectToMatcher[A](o: A): Matcher[A] = Matcher(_ == o)
  }
}
