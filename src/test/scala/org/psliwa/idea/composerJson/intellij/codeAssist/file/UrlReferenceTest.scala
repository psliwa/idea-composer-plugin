package org.psliwa.idea.composerJson.intellij.codeAssist.file

import org.junit.Assert._
import org.psliwa.idea.composerJson.ComposerJson
import org.psliwa.idea.composerJson.intellij.codeAssist.CompletionTest

class UrlReferenceTest extends CompletionTest {

  def testGivenUrlProperty_givenUrlValue_valueShouldBeUrlReference(): Unit = {
    checkUrlReference(
      """
        |{
        |  "homepage": "http://psliwa.org<caret>"
        |}
      """.stripMargin
    )
  }

  def testGivenUrlProperty_givenInvalidUrlValue_valueShouldNotBeUrlReference(): Unit = {
    checkUrlReference(
      """
        |{
        |  "homepage": "invalid<caret>"
        |}
      """.stripMargin,
      0
    )
  }

  def testGivenEmailProperty_givenEmailValue_valueShouldBeUrlReference(): Unit = {
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

  def testGivenEmailProperty_givenInvalidEmailValue_valueShouldNotBeUrlReference(): Unit = {
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

  def testGivenUrlProperty_givenUrlPropertyIsInFactOrProperty_givenValidUrl_valueShouldBeUrlReference(): Unit = {
    checkUrlReference(
      """
        |{
        |  "repositories": [
        |    {
        |      "url": "http://psliwa.org<caret>"
        |    }
        |  ]
        |}
      """.stripMargin
    )
  }

  def testGivenPackageVersionProperty_valueShouldBeUrlReference(): Unit = {
    checkUrlReference(
      """
        |{
        |  "require": {
        |    "some/pkg": "1.<caret>0.0"
        |  }
        |}
      """.stripMargin
    )
  }

  private def checkUrlReference(s: String, expectedCount: Int = 1): Unit = {
    myFixture.configureByText(ComposerJson, s)

    val element = myFixture.getFile.findElementAt(myFixture.getCaretOffset).getParent

    val references = element.getReferences
      .filter(_.isInstanceOf[UrlPsiReference])

    assertEquals(expectedCount, references.length)
  }
}
