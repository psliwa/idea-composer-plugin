package org.psliwa.idea.composerJson.composer.version

import org.psliwa.idea.composerJson.composer.version.{VersionGenerators => gen}
import org.scalacheck.Gen
import org.scalacheck.Prop.{forAll, BooleanOperators}
import org.scalatest.PropSpec
import org.scalatest.prop.Checkers

class VersionAlternativesTest extends PropSpec with Checkers {

  //generators

  def semanticVersionGen(prefix: String = ""): Gen[GeneratedVersion] =
    gen.semanticVersion(3).map(version => GeneratedVersion(prefix+version.presentation))

  def nonSemanticVersionGen: Gen[GeneratedVersion] =
    Gen.oneOf(gen.devVersion, gen.hashVersion).map(version => GeneratedVersion(version.presentation))

  def prefixGen = for {
    prefixVersion <- Gen.option(Gen.oneOf(semanticVersionGen(), nonSemanticVersionGen).map(_.get))
    prefix <- gen.constraintOperator.map(_.toString)
  } yield Prefix(prefix+prefixVersion.getOrElse(""))

  //properties

  property("alternatives for pure semantic version") {
    check(forAll(semanticVersionGen(prefix = "")) { (version: GeneratedVersion) =>
      val alternatives = Version.alternativesForPrefix("")(version.get)

      checkSemanticVersionAlternatives(version.get, alternatives)
    })
  }

  property("alternatives for pure semantic version and prefix") {
    check(forAll(prefixGen, semanticVersionGen(prefix = "")) { (prefix: Prefix, version: GeneratedVersion) =>
      val alternatives = Version.alternativesForPrefix(prefix.get)(version.get)
      val alternativesForPrefixedVersion = Version.alternativesForPrefix(prefix.get)(s"v${version.get}")

      alternatives.contains(version.get)                  :| "alternatives contain original version" &&
        (!alternatives.exists(_.contains('*')))           :| "alternatives don't contain wildcarded versions" &&
        (alternativesForPrefixedVersion == alternatives)  :| "alternatives for 'v' prefixed versions are the same"
    })
  }

  property("alternatives for prefixed semantic version") {
    check(forAll(semanticVersionGen(prefix = "v")) { (version: GeneratedVersion) =>
      val alternatives = Version.alternativesForPrefix("")(version.get)

      val pureSemanticVersion = version.get.drop(1)
      val alternativesWithoutOriginal = alternatives.filterNot(_ == version.get)

      checkSemanticVersionAlternatives(pureSemanticVersion, alternativesWithoutOriginal) &&
        alternatives.contains(version.get) :| "alternatives contain prefixed semantic version"
    })
  }

  property("alternatives for non semantic version") {
    check(forAll(nonSemanticVersionGen) { (version: GeneratedVersion) =>
      val alternatives = Version.alternativesForPrefix("")(version.get)

      alternatives.contains(version.get)    :| "alternatives contain original version" &&
        (alternatives.size == 1)            :| "alternatives contain only one alternative"
    })
  }

  def checkSemanticVersionAlternatives(version: String, alternatives: List[String]) = {
    val wildcards = wildcard(alternatives)

    alternatives.contains(version)                            :| "alternatives contain original pure semantic version" &&
      (alternatives.size == 3)                                :| s"there are 3 alternatives: $alternatives" &&
      (alternatives.distinct.size == 3)                       :| s"alternatives are unique: $alternatives" &&
      (wildcard(alternatives).size == 2)                      :| "there are 2 wildcard alternatives" &&
      wildcards.forall(_.endsWith(".*"))                      :| "wildcard alternatives end with *" &&
      wildcards.forall(matches(_, version))                   :| "all wildcard alternatives matches original one" &&
      wildcards.forall(_.startsWith(version.substring(0, 1))) :| "wildcard alternatives start as same as original one"
  }

  def wildcard(alternatives: List[String]) = alternatives.filter(_.contains("*"))
  def matches(wildcard: String, version: String) = {
    val regexp = ("^"+wildcard.replace(".", "\\.").replace("*", ".*")+"$").r

    regexp.findFirstMatchIn(version).isDefined
  }

  case class GeneratedVersion(get: String)
  case class Prefix(get: String)

}
