package org.psliwa.idea.composer.test.idea.CompletionContributor

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class CompletionTest extends LightPlatformCodeInsightFixtureTestCase {

  def testCompletionQuotedElementWithinQuotes() = {
    completion(
      """
        |{
        | "licens<caret>"
        |}
      """.stripMargin,
      """
        |{
        | "license<caret>"
        |}
      """.stripMargin
    )
  }

  def testCompletionQuotedElementOutsideQuotes() = {
    completion(
      """
        |{
        | licens<caret>
        |}
      """.stripMargin,
      """
        |{
        | "license"
        |}
      """.stripMargin
    )
  }

  def completion(contents: String, expected: String) = {
    myFixture.configureByText("composer.json", contents)
    myFixture.completeBasic()

    myFixture.checkResult(expected.replace("\r", ""))
  }
}
