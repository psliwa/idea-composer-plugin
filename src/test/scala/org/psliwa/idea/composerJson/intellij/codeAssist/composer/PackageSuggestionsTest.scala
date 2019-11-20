package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import org.junit.Assert._
import org.psliwa.idea.composerJson.intellij.codeAssist.{BaseLookupElement, CompletionTest}

class PackageSuggestionsTest extends AbstractPackagesTest {

  def testPackageSuggestions(): Unit = {
    val packages = List("some/package1", "some/package2")
    setCompletionPackageLoader(() => packages.map(new BaseLookupElement(_)))

    suggestions(
      """
        |{
        | "require": {
        |   <caret>
        | }
        |}
      """.stripMargin,
      packages.toArray
    )
  }

  def testPackageSuggestions_givenVendorAndSlash_suggestMatchingPackages(): Unit = {
    val packages = List("some/package1", "some/package2", "another/package3")
    setCompletionPackageLoader(() => packages.map(new BaseLookupElement(_)))

    suggestions(
      """
        |{
        | "require": {
        |   "some/<caret>"
        | }
        |}
      """.stripMargin,
      Array("some/package1", "some/package2"),
      Array("another/package3")
    )
  }

  def testVersionSuggestions(): Unit = {

    val pkg = "ps/image-optimizer"
    val versions = List("dev-master", "1.0.0")

    val map = Map(pkg -> versions)

    setCompletionPackageLoader(() => List(new BaseLookupElement(pkg)))
    setCompletionVersionsLoader(map.getOrElse(_, List()))

    suggestions(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "<caret>"
        | }
        |}
      """.stripMargin,
      versions.toArray
    )
  }

  def testVersionSuggestions_includeVersionWildcards(): Unit = {
    val versions = List("1.2.1")
    setCompletionVersionsLoader(_ => versions)

    suggestions(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "<caret>"
        | }
        |}
      """.stripMargin,
      Array("1.2.1", "1.2.*", "1.*")
    )
  }

  def testVersionsSuggestions_givenNsrOperator_suggestOnlySemanticVersions(): Unit = {
    val versions = List("1.2.1", "1.2.2", "v1.2.3", "dev-master")
    setCompletionVersionsLoader(_ => versions)

    suggestions(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "~<caret>"
        | }
        |}
      """.stripMargin,
      Array("1.2", "1.2.1", "1.2.2"),
      Array("v1.2.3", "dev-master")
    )
  }

  def testVersionsSuggestions_givenAsterixInPrefix_suggestAsterixWildcards(): Unit = {
    val versions = List("1.2.1", "1.3.2", "dev-master")
    setCompletionVersionsLoader(_ => versions)

    suggestions(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1*<caret>"
        | }
        |}
      """.stripMargin,
      Array("1.*", "1.2.*", "1.3.*"),
      Array("1.2.1", "1.3.2")
    )
  }

  def testVersionsSuggestions_givenSuggestionAfterSpace_allVersionsShouldBeSuggested(): Unit = {
    val versions = List("1.2.1", "1.3.2", "dev-master")
    setCompletionVersionsLoader(_ => versions)

    suggestions(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.1 <caret>"
        | }
        |}
      """.stripMargin,
      versions.toArray
    )
  }

  def testVersionsSuggestions_givenSuggestionAfterSpaceAndSomePrefix_validVersionsShouldBeSuggested(): Unit = {
    val versions = List("1.2.1", "1.3.1", "dev-master")
    setCompletionVersionsLoader(_ => versions)

    suggestions(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.1 12<caret>"
        | }
        |}
      """.stripMargin,
      Array("1.2.1"),
      Array("1.3.1")
    )
  }

  def testVersionsSuggestions_givenFewSemanticVersions_itShouldBeDescSorted(): Unit = {
    val versions = List("1.2.1", "1.3.1", "dev-master", /*"1.2",*/ "1.0.0")

    setCompletionVersionsLoader(_ => versions)

    orderedSuggestions(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "<caret>"
        | }
        |}
      """.stripMargin,
      Array("1.3.1", "1.2.1", "1.0.0", /*"1.2",*/ "dev-master")
    )
  }

  def testVersionsSuggestions_givenSuggestionsAfterSpaceWithTilda_semanticVersionsShouldBeSuggested(): Unit = {
    val versions = List("1.2.1", "1.3.1", "dev-master")
    setCompletionVersionsLoader(_ => versions)

    suggestions(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.1 ~<caret>"
        | }
        |}
      """.stripMargin,
      Array("1.2", "1.3", "1.2.1", "1.3.1"),
      Array("dev-master")
    )
  }

  def testVersionsSuggestions_givenSuggestionsAfterComma_allVersionsShouldBeSuggested(): Unit = {
    val versions = List("1.2.1", "1.3.1", "dev-master")
    setCompletionVersionsLoader(_ => versions)

    suggestions(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.1,<caret>"
        | }
        |}
      """.stripMargin,
      versions.toArray
    )
  }

  def testVersionsSuggestions_givenSuggestionsAfterComparisonChar_allSemanticVersionsShouldBeSuggested(): Unit = {
    val versions = List("1.2.1", "1.3.1", "dev-master")
    setCompletionVersionsLoader(_ => versions)

    for (prefix <- List(">", "<", ">=", "<=")) {
      suggestions(
        s"""
          |{
          | "require": {
          |   "ps/image-optimizer": "$prefix<caret>"
          | }
          |}
        """.stripMargin,
        Array("1.2.1", "1.3.1", "1.2", "1.3"),
        Array("dev-master", "1.2.*")
      )
    }
  }

  def testVersionSuggestions_givenSuggestionsAfterSpaceAndComparisonChar_allSemanticVersionsShouldBeSuggested()
      : Unit = {
    val versions = List("1.2.1", "1.3.1", "dev-master")
    setCompletionVersionsLoader(_ => versions)

    for (prefix <- List(">", "<", ">=", "<=")) {
      suggestions(
        s"""
          |{
          | "require": {
          |   "ps/image-optimizer": "1.2.1 $prefix<caret>"
          | }
          |}
        """.stripMargin,
        Array("1.2.1", "1.3.1", "1.2", "1.3"),
        Array("dev-master", "1.2.*")
      )
    }
  }

  def testVersionSuggestions_givenSuggestionAfterAtMark_stabilityShouldBeSuggested(): Unit = {
    val versions = List("1.2.1")
    setCompletionVersionsLoader(_ => versions)

    suggestions(
      """
        |{
        |  "require": {
        |    "vendor/package": "1.2.*@<caret>"
        |  }
        |}
      """.stripMargin,
      Array("dev", "alpha", "stable"),
      Array("1.2.1", "1.2.*@dev")
    )
  }

  def testVersionSuggestions_givenSuggestionAfterCommaAndAtMark_versionsShouldBeSuggested(): Unit = {
    val versions = List("1.2.1")
    setCompletionVersionsLoader(_ => versions)

    suggestions(
      """
        |{
        |  "require": {
        |    "vendor/package": "1.2.*@dev,<caret>"
        |  }
        |}
      """.stripMargin,
      Array("1.2.1"),
      Array("dev")
    )
  }

  def testPackageSuggestion_versionLoaderShouldNotBeCalled(): Unit = {
    setCompletionVersionsLoader(_ => { fail("version loader should not be called"); List() })

    suggestions(
      """
        |{
        | "require": {
        |   "<caret>"
        | }
        |}
      """.stripMargin,
      Array()
    )
  }
}
