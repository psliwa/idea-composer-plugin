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
      Array[String]("name", "type")
    )
  }

  private def completion(contents: String, expectedSuggestions: Array[String]) = {
    myFixture.configureByText("composer.json", contents)
    myFixture.completeBasic()

    assertContainsElements(myFixture.getLookupElementStrings, expectedSuggestions:_*)
  }

  def testEnumCompletion() = {
    completion(
      """
        | {
        |   "license": <caret>
        | }
      """.stripMargin,
      Array[String]("MIT", "BSD")
    )
  }
}
