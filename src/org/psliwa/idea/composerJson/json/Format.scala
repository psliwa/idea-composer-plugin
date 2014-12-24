package org.psliwa.idea.composerJson.json

import java.net.{MalformedURLException, URL}

trait Format {
  def isValid(s: String): Boolean
}

object EmailFormat extends Format {
  override def isValid(s: String): Boolean = "^(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$".r.findFirstMatchIn(s).isDefined
}

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

object AnyFormat extends Format{
  override def isValid(s: String) = true
}