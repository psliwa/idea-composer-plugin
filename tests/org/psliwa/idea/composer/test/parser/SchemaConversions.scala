package org.psliwa.idea.composer.test.parser

import org.psliwa.idea.composer.parser._
import scala.language.implicitConversions

object SchemaConversions {
  implicit def stringProp(s: String): (String, Schema) = (s,SString)
}
