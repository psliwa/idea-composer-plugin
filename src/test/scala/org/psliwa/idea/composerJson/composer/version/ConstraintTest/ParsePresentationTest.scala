package org.psliwa.idea.composerJson.composer.version.ConstraintTest

import org.psliwa.idea.composerJson.composer.version.{VersionGenerators => gen, _}
import org.scalacheck.Prop.{BooleanOperators, forAll}
import org.scalatest.PropSpec
import org.scalatest.prop.Checkers

class ParsePresentationTest extends PropSpec with Checkers {

  property("presentation should be able to be parsed") {
    check(forAll(gen.version) { (version: Constraint) =>
      val parsed = Parser.parse(version.presentation)

      parsed.contains(version) :| s"$parsed contains $version, presentation ${version.presentation}"
    }, MinSuccessful(500))
  }

}
