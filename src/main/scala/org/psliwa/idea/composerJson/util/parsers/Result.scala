package org.psliwa.idea.composerJson.util.parsers

sealed trait Result[+A] {
  def advanceSuccess(n: Int): Result[A] = this match {
    case Success(a, c) => Success(a, c+n)
    case _ => this
  }
}
case object Failure extends Result[Nothing]
case class Success[+A](get: A, charsConsumed: Int) extends Result[A]