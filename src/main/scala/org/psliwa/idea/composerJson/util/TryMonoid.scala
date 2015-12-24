package org.psliwa.idea.composerJson.util

import scala.util.{Failure, Try}
import scalaz._

class TryMonoid[A](ex: Throwable) extends Monoid[Try[A]] {
  override def zero: Try[A] = Failure(ex)
  override def append(f1: Try[A], f2: => Try[A]): Try[A] = f1.recoverWith { case _ => f2 }
}
