package org.psliwa.idea.composerJson.util.parsers

import scala.language.implicitConversions
import scala.util.matching.Regex
import scalaz.Monad

object Implicits {
  implicit def string(s: String): Parser[String] = Parsers.string(s)
  implicit def regex(r: Regex): Parser[String] = Parsers.regex(r)
  implicit def asStringParser[A](a: A)(implicit f: A => Parser[String]): ParserOps[String] = new ParserOps(f(a))
  implicit def ToParserOps[A](p: Parser[A]): ParserOps[A] = new ParserOps(p)
  implicit val monad: Monad[Parser] = ParserMonad
}
