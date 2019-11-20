package org.psliwa.idea.composerJson.util.parsers

import scalaz.Monad

object ParserMonad extends Monad[Parser] {

  override def bind[A, B](p: Parser[A])(f: (A) => Parser[B]): Parser[B] = loc => {
    p(loc) match {
      case Success(a, n) => f(a)(loc.advancedBy(n)).advanceSuccess(n)
      case Failure => Failure
    }
  }

  override def point[A](a: => A): Parser[A] = input => Success(a, 0)

  def fail[A](): Parser[A] = input => Failure

  def succeed[A](a: A): Parser[A] = point(a)

}
