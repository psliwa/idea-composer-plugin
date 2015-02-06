package org.psliwa.idea.composerJson.util

import scala.language.implicitConversions

object ImplicitConversions {
  implicit def wrapString(s: String): StringOps = new StringOps(s)
}
