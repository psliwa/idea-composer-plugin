package org.psliwa.idea.composerJson.util.parsers

sealed trait Result[+A]
case object Failure extends Result[Nothing]
case class Success[+A](get: A, charsConsumed: Int) extends Result[A]