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

  private def completion(contents: String, expectedSuggestions: Array[String]) = {
    myFixture.configureByText("composer.json", contents)
    myFixture.completeBasic()

    assertContainsElements(myFixture.getLookupElementStrings, expectedSuggestions:_*)
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
      Array("library", "project")
    )
  }

  def testEnumCompletionWithLandingColon() = {
    completion(
      """
        | {
        |   "type": <caret>,
        | }
      """.stripMargin,
      Array("library", "project")
    )
  }

  def testEnumCompletionWithQuotes() = {
    completion(
      """
        | {
        |   "type": "<caret>"
        | }
      """.stripMargin,
      Array("library", "project")
    )
  }

  def testEnumCompletionWithQuotesAndLandingColon() = {
    completion(
      """
        | {
        |   "type": "<caret>",
        | }
      """.stripMargin,
      Array("library", "project")
    )
  }
}
