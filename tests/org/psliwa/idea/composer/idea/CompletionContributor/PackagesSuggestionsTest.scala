package org.psliwa.idea.composer.idea.completionContributor

import org.psliwa.idea.composer.idea.Keyword

class PackagesSuggestionsTest extends SuggestionsTest {

  def testPackageSuggestions() = {
    val packages = List("some/package1", "some/package2")
    setCompletionPackageLoader(() => packages.map(Keyword(_)))

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

  def testVersionSuggestions() = {

    val pkg = "ps/image-optimizer"
    val versions = List("dev-master", "1.0.0")

    val map = Map(pkg -> versions)

    setCompletionPackageLoader(() => List(Keyword(pkg)))
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

  def testVersionSuggestions_includeVersionWildcards() = {
    val pkg = "ps/image-optimizer"
    val versions = List("1.2.1")

    val map = Map(pkg -> versions)

    setCompletionPackageLoader(() => List(pkg).map(Keyword(_)))
    setCompletionVersionsLoader(map.getOrElse(_, List()))

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

  def testVersionsSuggestions_tildeGiven_suggestOnlySemanticVersionWithTwoNumbers() = {
    val pkg = "ps/image-optimizer"
    val versions = List("1.2.1", "1.2.2", "v1.2.3", "dev-master")

    val map = Map(pkg -> versions)

    setCompletionPackageLoader(() => List(pkg).map(Keyword(_)))
    setCompletionVersionsLoader(map.getOrElse(_, List()))

    suggestions(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "~<caret>"
        | }
        |}
      """.stripMargin,
      Array("1.2"),
      versions.toArray
    )
  }

  def testVersionsSuggestions_givenAsterixInPrefix_suggestAsterixWildcards() = {
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

  def testVersionsSuggestions_givenSuggestionAfterSpace_allVersionsShouldBeSuggested() = {
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

  def testVersionsSuggestions_givenSuggestionAfterSpaceAndSomePrefix_validVersionsShouldBeSuggested() = {
    val versions = List("1.2.1", "1.3.1", "dev-master")
    setCompletionVersionsLoader(_ => versions)



  }

  def testVersionsSuggestions_givenSuggestionsAfterSpaceWithTilda_semanticVersionsShouldBeSuggested() = {
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
      Array("1.2", "1.3"),
      versions.toArray
    )
  }

  def testVersionsSuggestions_givenSuggestionsAfterComma_allVersionsShouldBeSuggested() = {
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
}