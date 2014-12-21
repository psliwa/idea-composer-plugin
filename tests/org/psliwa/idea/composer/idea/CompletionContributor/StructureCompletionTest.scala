package org.psliwa.idea.composer.idea.completionContributor

class StructureCompletionTest extends TestCase {



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

  def testCompletionBooleanPropertyIsNotLast_commaShouldBeAdded() = {
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

  def testCompletionStringPropertyOutsideQuotes_givenPropertyHasPreviousSiblingStringProperty_quotesShouldBeFixed() = {
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
