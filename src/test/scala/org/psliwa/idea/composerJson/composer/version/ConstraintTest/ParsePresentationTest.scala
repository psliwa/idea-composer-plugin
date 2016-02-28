package org.psliwa.idea.composerJson.composer.version.ConstraintTest

import org.psliwa.idea.composerJson.composer.version._
import org.scalacheck.Prop
import Prop.BooleanOperators
import org.psliwa.idea.composerJson.composer.version.{VersionGenerators => gen}
import org.scalatest.PropSpec
import org.scalatest.prop.PropertyChecks

class ParsePresentationTest extends PropSpec with PropertyChecks {

  property("presentation should be able to be parsed") {
    forAll(gen.version) { (version: Constraint) =>
      val parsed = Parser.parse(version.presentation)

      parsed.contains(version) :| s"$parsed contains $version, presentation ${version.presentation}"
    }
  }

}
