package org.psliwa.idea.composerJson.util.parsers

import org.psliwa.idea.composerJson.util.parsers.Implicits._

import scala.language.implicitConversions
import scalaz.Monad

class ParserOps[A](p: Parser[A]) {

  def run(input: String): Option[A] = p(Location(input)) match {
    case Success(a,_) => Some(a)
    case Failure => None
  }

  def or[B >: A](p2: Parser[B]): Parser[B] = input => {
    p(input) match {
      case Failure => p2(input)
      case x => x
    }
  }

  def |[B >: A](p2: Parser[B]): Parser[B] = or(p2)

  def many: Parser[List[A]] = Monad[Parser].apply2(p, p.many)(_ :: _) or Monad[Parser].point(List[A]())

  def map[B](f: A => B): Parser[B] = Monad[Parser].map(p)(f)

  def flatMap[B](f: A => Parser[B]): Parser[B] = Monad[Parser].bind(p)(f)

}

trait ToParserOps {
  implicit def ToParserOps[A](p: Parser[A]): ParserOps[A] = new ParserOps(p)
}
