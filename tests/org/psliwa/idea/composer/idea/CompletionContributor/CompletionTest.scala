package org.psliwa.idea.composer.idea.completionContributor

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.psliwa.idea.composer.idea.Keyword

class CompletionTest extends LightPlatformCodeInsightFixtureTestCase with TestCase {

  def testCompletionQuotedStringProperty_propertyValueShouldBeCompleted() = {
    completion(
      """
        |{
        | "nam<caret>"
        |}
      """.stripMargin,
      """
        |{
        | "name": "<caret>"
        |}
      """.stripMargin
    )
  }

  def testCompletionQuotedStringPropertyOutsideQuotes_quotesShouldBeFixed_propertyValueShouldBeCompleted() = {
    completion(
      """
        |{
        | nam<caret>
        |}
      """.stripMargin,
      """
        |{
        | "name": "<caret>"
        |}
      """.stripMargin
    )
  }

  def testCompletionObjectProperty_curlyBracketsShouldBeFixed() = {
    completion(
      """
        |{
        | "confi<caret>"
        |}
      """.stripMargin,
      """
        |{
        | "config": {<caret>}
        |}
      """.stripMargin
    )
  }

  def testCompletionArrayProperty_squareBracketsShouldBeFixed() = {
    completion(
      """
        |{
        | "includepat<caret>"
        |}
      """.stripMargin,
      """
        |{
        | "include-path": [<caret>]
        |}
      """.stripMargin
    )
  }

  def testCompletionPackageProperty_curlyBracketsShouldBeFixed() = {
    completion(
      """
        |{
        | "requirede<caret>"
        |}
      """.stripMargin,
      """
        |{
        | "require-dev": {<caret>}
        |}
      """.stripMargin
    )
  }

  def testCompletionPackageName_quotesShouldBeFixed() = {
    val contributor = getCompletionContributor

    val pkg = "ps/image-optimizer"
    contributor.setPackagesLoader(() => List(Keyword(pkg)))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-opti<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "<caret>"
        | }
        |}
      """.stripMargin
    )
  }

  def testCompletionBooleanProperty_colonShouldBeAdded() = {
    completion(
      """
        |{
        | "prefer-stab<caret>"
        |}
      """.stripMargin,
      """
        |{
        | "prefer-stable": <caret>
        |}
      """.stripMargin
    )
  }

  def testCompletionOrProperty_chooseFirstAlternativeAsBaseOfFix() = {
    completion(
      """
        |{
        | "licens<caret>"
        |}
      """.stripMargin,
      """
        |{
        | "license": "<caret>"
        |}
      """.stripMargin
    )
  }

  def testCompletedPropertyInObjectIsNotLast_commaShouldBeAdded() = {
    completion(
      """
        |{
        | "nam<caret>"
        | "library": "MIT"
        |}
      """.stripMargin,
      """
        |{
        | "name": "<caret>",
        | "library": "MIT"
        |}
      """.stripMargin
    )
  }

  def testCompletionBooleanPropertyIsNotLast_colonShouldBeAdded() = {
    completion(
      """
        |{
        | "prefer-stab<caret>"
        | "library": "MIT"
        |}
      """.stripMargin,
      """
        |{
        | "prefer-stable": <caret>,
        | "library": "MIT"
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
