package org.psliwa.idea.composerJson.composer

import scala.language.implicitConversions

object SchemaConversions {
  implicit def stringProp(s: String): (String, Schema) = (s,SString)
}
