package org.psliwa.idea.composerJson.composer

import scala.language.implicitConversions

object SchemaConversions {
  implicit def stringProp(s: String): (String, Property) = (s, Property(SString(AnyFormat), required = false))
  implicit def schemaToProperty(s: Schema): Property = Property(s, required = false)
}
