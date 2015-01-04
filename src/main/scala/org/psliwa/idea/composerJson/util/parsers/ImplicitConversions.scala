package org.psliwa.idea.composerJson.util.parsers

import org.psliwa.idea.composerJson.util.parsers.Parsers.ParserOps

import scala.language.implicitConversions
import scala.util.matching.Regex

object ImplicitConversions {
  implicit def string(s: String): Parser[String] = Parsers.string(s)
  implicit def regex(r: Regex): Parser[String] = Parsers.regex(r)
  implicit def operators[A](p: Parser[A]): ParserOps[A] = ParserOps(p)
  implicit def asStringParser[A](a: A)(implicit f: A => Parser[String]): ParserOps[String] = ParserOps(f(a))
}
