package org.psliwa.idea.composer.test.idea

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class CompletionContributorTest extends LightPlatformCodeInsightFixtureTestCase {

  def testCompletionOnTopLevel() = {
    completion(
      """
        | {
        | <caret>
        | }
      """.stripMargin,
      Array("name", "type")
    )
  }

  private def completion(contents: String, expectedSuggestions: Array[String], unexpectedSuggestions: Array[String] = Array()) = {
    myFixture.configureByText("composer.json", contents)
    myFixture.completeBasic()

    assertContainsElements(myFixture.getLookupElementStrings, expectedSuggestions:_*)
    assertDoesntContain(myFixture.getLookupElementStrings, unexpectedSuggestions:_*)
  }

  def testCompletionOnTopLevelWithQuotes() = {
    completion(
      """
        | {
        | "<caret>"
        | }
      """.stripMargin,
      Array("name", "type")
    )
  }

  def testEnumCompletion() = {
    completion(
      """
        | {
        |   "type": <caret>
        | }
      """.stripMargin,
      Array("library", "project"),
      Array("name", "type")
    )
  }

  def testEnumCompletionWithLandingColon() = {
    completion(
      """
        | {
        |   "type": <caret>,
        | }
      """.stripMargin,
      Array("library", "project"),
      Array("name", "type")
    )
  }

  def testEnumCompletionWithQuotes() = {
    completion(
      """
        | {
        |   "type": "<caret>"
        | }
      """.stripMargin,
      Array("library", "project"),
      Array("name", "type")
    )
  }

  def testEnumCompletionWithQuotesAndLandingColon() = {
    completion(
      """
        | {
        |   "type": "<caret>",
        | }
      """.stripMargin,
      Array("library", "project"),
      Array("name", "type")
    )
  }

  def testNestedObjectCompletion() = {
    completion(
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

  def testOrCompletion_firstAlternative() = {
    completion(
      """
        |{
        | "license": <caret>
        |}
      """.stripMargin,
      Array("MIT", "BSD")
    )
  }

  def testOrCompletion_secondAlternative() = {
    completion(
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

  def testBooleanCompletion() = {
    completion(
      """
        |{
        | "prefer-stable": <caret>
        |}
      """.stripMargin,
      Array("true", "false")
    )
  }

  def testArrayCompletion() = {
    completion(
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
