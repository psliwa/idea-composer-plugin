package org.psliwa.idea.composerJson.composer.version.ConstraintTest

import org.psliwa.idea.composerJson.composer.version._
import org.scalacheck.{Prop, Properties}
import Prop.BooleanOperators
import org.psliwa.idea.composerJson.composer.version.{VersionGenerators => gen}

class ParsePresentationTest extends Properties("Constraint") {

  property("presentation should be able to be parsed") = Prop.forAll(gen.version) { (version: Constraint) =>
    val parsed = Parser.parse(version.presentation)

    parsed.contains(version) :| s"$parsed contains $version, presentation ${version.presentation}"
  }

}
