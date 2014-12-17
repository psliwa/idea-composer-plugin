package org.psliwa.idea.composer.idea.completionContributor

import java.util.Collections

import com.intellij.codeInsight._
import com.intellij.json.JsonLanguage
import com.intellij.openapi.extensions.{Extensions, ExtensionPointName}
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.psliwa.idea.composer.idea.{Keyword, CompletionContributor}

class SuggestionsTest extends LightPlatformCodeInsightFixtureTestCase with TestCase {

  def testSuggestionsOnTopLevel() = {
    suggestions(
      """
        | {
        | <caret>
        | }
      """.stripMargin,
      Array("name", "type")
    )
  }

  private def suggestions(contents: String, expectedSuggestions: Array[String], unexpectedSuggestions: Array[String] = Array()) = {
    myFixture.configureByText("composer.json", contents)
    myFixture.completeBasic()

    val lookupElements = myFixture.getLookupElementStrings

    assertContainsElements(lookupElements, expectedSuggestions:_*)
    assertDoesntContain(lookupElements, unexpectedSuggestions:_*)
  }

  def testSuggestionsOnTopLevelWithQuotes() = {
    suggestions(
      """
        | {
        | "<caret>"
        | }
      """.stripMargin,
      Array("name", "type")
    )
  }

  def testSuggestionsOnTopLevelWithQuotes_givenPartialText() = {
    suggestions(
      """
        | {
        | "ty<caret>"
        | }
      """.stripMargin,
      Array("type"),
      Array("name")
    )
  }

  def testSuggestionsOnTopLevelWithoutQuotes_givenPartialText() = {
    suggestions(
      """
        | {
        | ty<caret>
        | }
      """.stripMargin,
      Array("type"),
      Array("name")
    )
  }

  def testEnumSuggestions() = {
    suggestions(
      """
        | {
        |   "type": <caret>
        | }
      """.stripMargin,
      Array("library", "project"),
      Array("name", "type")
    )
  }

  def testEnumSuggestionsWithLandingColon() = {
    suggestions(
      """
        | {
        |   "type": <caret>,
        | }
      """.stripMargin,
      Array("library", "project"),
      Array("name", "type")
    )
  }

  def testEnumSuggestionsWithQuotes() = {
    suggestions(
      """
        | {
        |   "type": "<caret>"
        | }
      """.stripMargin,
      Array("library", "project"),
      Array("name", "type")
    )
  }

  def testEnumSuggestionsWithQuotesAndLandingColon() = {
    suggestions(
      """
        | {
        |   "type": "<caret>",
        | }
      """.stripMargin,
      Array("library", "project"),
      Array("name", "type")
    )
  }

  def testNestedObjectSuggestions() = {
    suggestions(
      """
        |{
        | "support": {
        |   <caret>
        | }
        |}
      """.stripMargin,
      Array("email", "issues"),
      Array("type")
    )
  }

  def testOrSuggestions_firstAlternative() = {
    suggestions(
      """
        |{
        | "license": <caret>
        |}
      """.stripMargin,
      Array("MIT", "BSD")
    )
  }

  def testOrSuggestions_secondAlternative() = {
    suggestions(
      """
        |{
        | "license": [
        |   <caret>
        | ]
        |}
      """.stripMargin,
      Array("MIT", "BSD")
    )
  }

  def testBooleanSuggestions() = {
    suggestions(
      """
        |{
        | "prefer-stable": <caret>
        |}
      """.stripMargin,
      Array("true", "false")
    )
  }

  def testArraySuggestions() = {
    suggestions(
      """
        |{
        | "authors": [
        |   {
        |     <caret>
        |   }
        | ]
        |}
      """.stripMargin,
      Array("name", "email", "homepage")
    )
  }

  def testRequireSuggestions() = {
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

  def testRequireVersionsSuggestions() = {

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
}
