package org.psliwa.idea.composerJson.composer.version.ConstraintTest

import org.psliwa.idea.composerJson.BasePropSpec
import org.psliwa.idea.composerJson.composer.version.{VersionGenerators => gen, _}
import org.scalacheck.Prop.{BooleanOperators, forAll}

class ParsePresentationTest extends BasePropSpec {

  property("presentation should be able to be parsed")({
    forAll(gen.version) { (version: Constraint) =>
      val parsed = Parser.parse(version.presentation)

      parsed.contains(version) :| s"$parsed contains $version, presentation ${version.presentation}"
    }
  }, MinSuccessful(500))

}
