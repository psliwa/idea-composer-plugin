package org.psliwa.idea.composerJson.util.parsers

import scala.language.implicitConversions
import scala.util.matching.Regex
import ImplicitConversions._

object Parsers { self =>

  def string(s: String): Parser[String] = loc => {
    if(loc.input.startsWith(s)) Success(s, s.length)
    else Failure
  }

  def regex(r: Regex): Parser[String] = loc => {
    r.findFirstMatchIn(loc.input)
      .map(m => Success(m.toString(),m.end))
      .getOrElse(Failure)
  }

  def char(c: Char): Parser[Char] = string(c.toString).map(_.charAt(0))

  case class ParserOps[A](p: Parser[A]) {
    def |[B>:A](p2: Parser[B]): Parser[B] = Combinators.or(p, p2)
    def or[B>:A](p2: Parser[B]): Parser[B] = Combinators.or(p, p2)
    def many: Parser[List[A]] = Combinators.many(p)
    def many1: Parser[List[A]] = Combinators.many1(p)
    def map[B](f: A => B): Parser[B] = Combinators.map(p)(f)
    def slice = Combinators.slice(p)
    def flatMap[B](f: A => Parser[B]): Parser[B] = Combinators.flatMap(p)(f)
    def product[B](p2: Parser[B]) = Combinators.product(p, p2)
    def **[B](p2: Parser[B]) = Combinators.product(p, p2)
  }
}
