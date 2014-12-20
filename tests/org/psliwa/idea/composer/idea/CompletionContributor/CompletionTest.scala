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

  def testCompletionQuotedStringProperty_propertyValueExists_caretShouldBeMovedToPropertyValue() = {
    completion(
      """
        |{
        | "nam<caret>": "someValue"
        |}
      """.stripMargin,
      """
        |{
        | "name": "<caret><selection>someValue</selection>"
        |}
      """.stripMargin
    )
  }

  def testCompletionQuotedStringProperty_propertyValueExists_thisIsNotLastPropertyInObject_caretShouldBeMovedToPropertyValue() = {
    completion(
      """
        |{
        | "nam<caret>": "someValue",
        | "some": "value"
        |}
      """.stripMargin,
      """
        |{
        | "name": "<caret><selection>someValue</selection>",
        | "some": "value"
        |}
      """.stripMargin
    )
  }

  def testCompletionStringProperty_unexpectedObjectPropertyValueExists_caretShouldBeMovedToPropertyValue() = {
    completion(
      """
        |{
        | "nam<caret>": { "abc": "value" }
        |}
      """.stripMargin,
      """
        |{
        | "name": <caret><selection>{ "abc": "value" }</selection>
        |}
      """.stripMargin
    )
  }

  def testCompletionStringProperty_unexpectedNestedObjectPropertyValueExists_caretShouldBeMovedToPropertyValue() = {
    completion(
      """
        |{
        | "nam<caret>": {
        |   "abc": {
        |     "prop": "val"
        |   }
        | }
        |}
      """.stripMargin,
      """
        |{
        | "name": <caret><selection>{
        |   "abc": {
        |     "prop": "val"
        |   }
        | }</selection>
        |}
      """.stripMargin
    )
  }

  def testCompletionStringProperty_unexpectedArrayPropertyValueExists_caretShouldBeMovedToPropertyValue() = {
    completion(
      """
        |{
        | "nam<caret>": [ "abc" ]
        |}
      """.stripMargin,
      """
        |{
        | "name": <caret><selection>[ "abc" ]</selection>
        |}
      """.stripMargin
    )
  }

  def testCompletionStringProperty_unexpectedLiteralPropertyValueExists_caretShouldBeMovedToPropertyValue() = {
    completion(
      """
        |{
        | "nam<caret>": true
        |}
      """.stripMargin,
      """
        |{
        | "name": <caret><selection>true</selection>
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

  def testCompletionPackageVersion_givenPrefix_quotesShouldBeStillValid() = {
    val pkg = "ps/image-optimizer"

    setCompletionPackageLoader(() => List(Keyword(pkg)))
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "~1<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "~1.2<caret>"
        | }
        |}
      """.stripMargin
    )
  }

  def testCompletionPackageVersion_givenPrefixWithSpace_completeVersion() = {
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2 123<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2 1.2.3"
        | }
        |}
      """.stripMargin
    )
  }

  def testCompletionPackageVersion_givenPrefixWithSpaceAndTilda_completeVersion() = {
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2 ~12<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2 ~1.2"
        | }
        |}
      """.stripMargin
    )
  }

  def testCompletionPackageVersion_givenPrefixWithComma_completeVersion() = {
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2,123<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2,1.2.3"
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
