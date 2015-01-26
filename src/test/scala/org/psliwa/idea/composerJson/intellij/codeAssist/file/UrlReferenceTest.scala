package org.psliwa.idea.composerJson.intellij.codeAssist.file

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.junit.Assert._
import org.psliwa.idea.composerJson.ComposerJson


class UrlReferenceTest extends LightPlatformCodeInsightFixtureTestCase {

  def testGivenUrlProperty_givenUrlValue_valueShouldBeUrlReference() = {
    checkUrlReference(
      """
        |{
        |  "homepage": "http://psliwa.org<caret>"
        |}
      """.stripMargin
    )
  }

  def testGivenUrlProperty_givenInvalidUrlValue_valueShouldNotBeUrlReference() = {
    checkUrlReference(
      """
        |{
        |  "homepage": "invalid<caret>"
        |}
      """.stripMargin,
      0
    )
  }

  def testGivenEmailProperty_givenEmailValue_valueShouldBeUrlReference() = {
    checkUrlReference(
      """
        |{
        |  "support": {
        |    "email": "me@psliwa.org<caret>"
        |  }
        |}
      """.stripMargin
    )
  }

  def testGivenEmailProperty_givenInvalidEmailValue_valueShouldNotBeUrlReference() = {
    checkUrlReference(
      """
        |{
        |  "support": {
        |    "email": "invalid<caret>"
        |  }
        |}
      """.stripMargin,
      0
    )
  }

  private def checkUrlReference(s: String, expectedCount: Int = 1) = {
    myFixture.configureByText(ComposerJson, s)

    val element = myFixture.getFile.findElementAt(myFixture.getCaretOffset).getParent

    val references = element.getReferences
      .filter(_.isInstanceOf[UrlPsiReference])

    assertEquals(expectedCount, references.size)
  }
}
