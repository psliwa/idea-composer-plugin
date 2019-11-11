package org.psliwa.idea.composerJson.composer.version

import org.psliwa.idea.composerJson.BasePropSpec
import org.psliwa.idea.composerJson.composer.version.{VersionGenerators => gen}
import org.scalacheck.{Gen, Prop}
import org.scalacheck.Prop.{BooleanOperators, forAll}

class VersionSuggestionsTest extends BasePropSpec {

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

  property("suggestions for pure semantic version") {
    forAll(semanticVersionGen(prefix = "")) { (version: GeneratedVersion) =>
      val suggestions = VersionSuggestions.suggestionsForVersion(version.get, "")

      checkSemanticVersionAlternatives(version.get, suggestions)
    }
  }

  property("suggestions for pure semantic version and prefix") {
    forAll(prefixGen, semanticVersionGen(prefix = "")) { (prefix: Prefix, version: GeneratedVersion) =>
      val suggestions = VersionSuggestions.suggestionsForVersion(version.get, prefix.get)
      val suggestionsForPrefixedVersion = VersionSuggestions.suggestionsForVersion(s"v${version.get}", prefix.get)

      suggestions.contains(version.get)                  :| "suggestions contain original version" &&
        (!suggestions.exists(_.contains('*')))           :| "suggestions don't contain wildcarded versions" &&
        (suggestionsForPrefixedVersion == suggestions)   :| "suggestions for 'v' prefixed versions are the same"
    }
  }

  property("suggestions for prefixed semantic version") {
    forAll(semanticVersionGen(prefix = "v")) { (version: GeneratedVersion) =>
      val suggestions = VersionSuggestions.suggestionsForVersion(version.get, "")

      val pureSemanticVersion = version.get.drop(1)
      val suggestionsWithoutOriginal = suggestions.filterNot(_ == version.get)

      checkSemanticVersionAlternatives(pureSemanticVersion, suggestionsWithoutOriginal) &&
        suggestions.contains(version.get) :| "suggestions contain prefixed semantic version"
    }
  }

  property("suggestions for non semantic version") {
    forAll(nonSemanticVersionGen) { (version: GeneratedVersion) =>
      val suggestions = VersionSuggestions.suggestionsForVersion(version.get, "")

      suggestions.contains(version.get)    :| "suggestions contain original version" &&
        (suggestions.size == 1)            :| "suggestions contain only one suggestion"
    }
  }

  def checkSemanticVersionAlternatives(version: String, suggestions: Seq[String]): Prop = {
    val wildcards = wildcard(suggestions)

    suggestions.contains(version)                             :| "suggestions contain original pure semantic version" &&
      (suggestions.size == 3)                                 :| s"there are 3 suggestions: $suggestions" &&
      (suggestions.distinct.size == 3)                        :| s"suggestions are unique: $suggestions" &&
      (wildcard(suggestions).size == 2)                       :| "there are 2 wildcard suggestions" &&
      wildcards.forall(_.endsWith(".*"))                      :| "wildcard suggestions end with *" &&
      wildcards.forall(matches(_, version))                   :| "all wildcard suggestions matches original one" &&
      wildcards.forall(_.startsWith(version.substring(0, 1))) :| "wildcard suggestions start as same as original one"
  }

  def wildcard(suggestions: Seq[String]): Seq[String] = suggestions.filter(_.contains("*"))
  def matches(wildcard: String, version: String): Boolean = {
    val regexp = ("^"+wildcard.replace(".", "\\.").replace("*", ".*")+"$").r

    regexp.findFirstMatchIn(version).isDefined
  }

  case class GeneratedVersion(get: String)
  case class Prefix(get: String)

}
