package org.psliwa.idea.composer.schema

import org.psliwa.idea.composer.schema._
import scala.language.implicitConversions

object SchemaConversions {
  implicit def stringProp(s: String): (String, Schema) = (s,SString)
}
