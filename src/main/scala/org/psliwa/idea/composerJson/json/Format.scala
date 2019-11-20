package org.psliwa.idea.composerJson.json

import java.net.{MalformedURLException, URL}

import scala.util.matching.Regex

trait Format {
  def isValid(s: String): Boolean
}

class PatternFormat(private val pattern: Regex) extends Format {
  override def isValid(s: String): Boolean = pattern.findFirstMatchIn(s).isDefined
}

object PatternFormat {
  def unapply(format: PatternFormat): Option[Regex] = Some(format.pattern)
}

object EmailFormat extends PatternFormat("^(?i)[\\p{L}0-9._%+-]+@[\\p{L}0-9.-]+\\.[\\p{L}0-9]{2,}$".r)

object UriFormat extends Format {
  override def isValid(s: String): Boolean = {
    try {
      new URL(s)
      true
    } catch {
      case _: MalformedURLException => false
    }
  }
}

object AnyFormat extends Format {
  override def isValid(s: String) = true
}
