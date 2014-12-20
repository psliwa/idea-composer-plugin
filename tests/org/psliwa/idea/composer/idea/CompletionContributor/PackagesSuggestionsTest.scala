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
}
