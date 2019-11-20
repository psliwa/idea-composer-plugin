package org.psliwa.idea.composerJson.intellij.codeAssist.schema

import org.psliwa.idea.composerJson.intellij.codeAssist.CompletionTest

class StructureCompletionTest extends CompletionTest {

  def testCompletionQuotedStringProperty_propertyValueShouldBeCompleted(): Unit = {
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

  def testCompletionQuotedStringProperty_propertyValueExists_caretShouldBeMovedToPropertyValue(): Unit = {
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

  def testCompletionQuotedStringProperty_propertyValueExists_thisIsNotLastPropertyInObject_caretShouldBeMovedToPropertyValue()
      : Unit = {
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

  def testCompletionStringProperty_unexpectedObjectPropertyValueExists_caretShouldBeMovedToPropertyValue(): Unit = {
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

  def testCompletionStringProperty_unexpectedNestedObjectPropertyValueExists_caretShouldBeMovedToPropertyValue()
      : Unit = {
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

  def testCompletionStringProperty_unexpectedArrayPropertyValueExists_caretShouldBeMovedToPropertyValue(): Unit = {
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

  def testCompletionStringProperty_unexpectedLiteralPropertyValueExists_caretShouldBeMovedToPropertyValue(): Unit = {
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

  def testCompletionQuotedStringPropertyOutsideQuotes_quotesShouldBeFixed_propertyValueShouldBeCompleted(): Unit = {
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

  def testCompletionObjectProperty_curlyBracketsShouldBeFixed(): Unit = {
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

  def testCompletionArrayProperty_squareBracketsShouldBeFixed(): Unit = {
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

  def testCompletionPackageProperty_curlyBracketsShouldBeFixed(): Unit = {
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

  def testCompletionBooleanProperty_colonShouldBeAdded(): Unit = {
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

  def testCompletionOrProperty_chooseFirstAlternativeAsBaseOfFix(): Unit = {
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

  def testCompletedPropertyInObjectIsNotLast_commaShouldBeAdded(): Unit = {
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

  def testCompletionBooleanPropertyIsNotLast_commaShouldBeAdded(): Unit = {
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

  def testCompletionStringPropertyOutsideQuotes_givenPropertyHasPreviousSiblingStringProperty_quotesShouldBeFixed()
      : Unit = {
    completion(
      """
        |{
        | "library": "MIT",
        | nam<caret>
        |}
      """.stripMargin,
      """
        |{
        | "library": "MIT",
        | "name": "<caret>"
        |}
      """.stripMargin
    )
  }
}
