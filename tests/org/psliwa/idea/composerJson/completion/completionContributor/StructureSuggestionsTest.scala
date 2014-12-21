package org.psliwa.idea.composerJson.completion.completionContributor

class StructureSuggestionsTest extends TestCase {

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
}
