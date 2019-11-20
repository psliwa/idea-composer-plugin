package org.psliwa.idea.composerJson.util.parsers

import scala.language.implicitConversions
import scala.util.matching.Regex
import Implicits._

object Parsers { self =>

  def string(s: String): Parser[String] = loc => {
    if (loc.input.startsWith(s)) Success(s, s.length)
    else Failure
  }

  def whole(): Parser[String] = loc => Success(loc.input, loc.input.length)

  def regex(r: Regex): Parser[String] = loc => {
    r.findFirstMatchIn(loc.input)
      .map(m => Success(m.toString(), m.end))
      .getOrElse(Failure)
  }

  def char(c: Char): Parser[Char] = string(c.toString).map(_.charAt(0))

}
