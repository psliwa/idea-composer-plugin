package org.psliwa.idea.composerJson.intellij.codeAssist.schema

import org.junit.ComparisonFailure
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest

class SchemaInspectionTest extends InspectionTest {

  private val RequiredProperties =
    """
      |"name": "vendor/pkg",
      |"description": "desc",
    """.stripMargin

  val AlreadyDefinedPropertyError = ComposerBundle.message("inspection.schema.alreadyDefinedProperty", _: String)

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[SchemaInspection])
  }

  def testReportNotAllowedPropertyOnTopLevel(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  <error descr="The 'unsupported' property is not allowed here.">"unsupported": "value"</error>
        |}
      """.stripMargin
    )
  }

  def testReportValueThatDoesNotMatchRequiredPattern(): Unit = {
    checkInspection(
      s"""
         |{
         |  "name": <error>"some"</error>,
         |  "description": "desc"
         |}
       """.stripMargin
    )
  }

  def testReportNotAllowedPropertyInNestedLevel(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "authors": [
        |    {
        |      "name": "some name",
        |      <error descr="The 'unsupported' property is not allowed here.">"unsupported": "value"</error>
        |    }
        |  ]
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueInEnum(): Unit = {

    val allowed = List("dev", "alpha", "beta", "rc", "RC", "stable").map("'" + _ + "'").mkString(" or ")

    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "minimum-stability": <error descr="Value 'invalid' is not allowed here, it must be $allowed.">"invalid"</error>
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportValidPropertyValueInEnum(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "type": "library"
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueInBooleanProperty(): Unit = {
    checkInspection(
      s"""
         |{
         |  $RequiredProperties
         |  "prefer-stable": <error descr="The value must be a boolean, string given.">"false"</error>
         |}
       """.stripMargin
    )
  }

  def testReportInvalidArrayPropertyValueInStringProperty(): Unit = {
    checkInspection(
      s"""
        |{
        |  "name": <error descr="The value must be a string, array given.">[ "some value" ]</error>,
        |  "description": "some desc"
        |}
      """.stripMargin
    )
  }

  def testReportInvalidObjectPropertyValueInStringProperty(): Unit = {
    checkInspection(
      s"""
        |{
        |  "name": <error descr="The value must be a string, object given.">{ "some name": "some value" }</error>,
        |  "description": "some desc"
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueInObjectProperty(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "config": <error descr="The value must be an object, string given.">"value"</error>
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueTypeInEnumProperty(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "type": <error descr="The value must be a string, object given.">{}</error>
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportErrorWhenObjectPropertyHasNotValueYet(): Unit = {
    try {
      checkInspection(
        s"""
        |{
        |  $RequiredProperties
        |  "type":
        |}
      """.stripMargin
      )
    } catch {
      case _: ComparisonFailure => //ignore syntax error assertionError, check only scala MatchFailure
    }
  }

  def testReportInvalidItemValueInArray(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "authors": [
        |    { "name": "psliwa" },
        |    <error descr="The value must be an object, string given.">"invalid"</error>
        |  ]
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueInArray(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "authors": <error descr="The value must be an array, object given.">{}</error>
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueInOrSchema(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "license": <error descr="The value must be a string or array, object given.">{ "some": "value" }</error>
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueInNumericProperty(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "config": {
        |    "process-timeout": <error descr="The value must be an integer, string given.">"invalid value"</error>
        |  }
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportValidPropertyValueInNumericProperty(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "config": {
        |    "process-timeout": 123
        |  }
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueInPackages(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "require": <error descr="The value must be an object, array given.">[]</error>
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportValidPropertyValueInPackages(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "require": {}
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportValidPropertyValueInArray(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "bin": [
        |    "some/value1/", "some/value2"
        |  ]
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportValidExtraProperty(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "extra": {
        |    "prop1": "value1",
        |    "prop2": "value2"
        |  }
        |}
      """.stripMargin
    )

    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "extra": [
        |    "value1", "value2"
        |  ]
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportValidRequireWithPackagesAndVersions(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "require": {
        |    "symfony/symfony": "2.5.0"
        |  }
        |}
      """.stripMargin
    )
  }

  def testReportInvalidStringPropertyValue(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "support": {
        |    "email": <error descr="The value must be an email.">"invalid"</error>
        |  }
        |}
      """.stripMargin
    )
  }

  def testNotReportCorrectEmailValue(): Unit = {
    checkInspection(
      s"""
         |{
         |  $RequiredProperties
         |  "support": {
         |    "email": "me@psliwa.org"
         |  }
         |}
      """.stripMargin
    )
  }

  def testNotReportCorrectEmailWithNonAsciiCharsValue(): Unit = {
    checkInspection(
      s"""
         |{
         |  $RequiredProperties
         |  "support": {
         |    "email": "mój@ąśćć.みんな"
         |  }
         |}
      """.stripMargin
    )
  }

  def testReportMissingProperties(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "authors": [
        |    <error descr="The 'name' property is required.">{}</error>
        |  ]
        |}
      """.stripMargin
    )
  }

  def testReportMissingPropertiesOnDeeperLevel(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "authors": [<error descr="The 'name' property is required.">{
        |    "role": "some role"
        |  }</error>]
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportWhenStringPropertyValueIsEmptyString(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "target-dir": ""
        |}
      """.stripMargin
    )
  }

  def testReportAlreadyDefinedProperty(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "require": {},
        |  <error descr="${AlreadyDefinedPropertyError("require")}">"require": {}</error>
        |}
      """.stripMargin
    )
  }

  def testInspectionObjectWithPatternProperties(): Unit = {
    checkInspection(
      s"""
         |{
         |  $RequiredProperties
         |  "repositories": {
         |    "name": {
         |      "type": <error>123</error>
         |    }
         |  }
         |}
       """.stripMargin
    )
  }

  def testValidEnumValueForMultipleOrs(): Unit = {
    checkInspection(
      s"""
         |{
         |  $RequiredProperties
         |  "repositories": {
         |    "name": {
         |      "type": "composer"
         |    }
         |  }
         |}
       """.stripMargin
    )
  }

  def testInvalidEnumValueForMultipleAlternatives(): Unit = {
    checkInspection(
      s"""
         |{
         |  $RequiredProperties
         |  "repositories": {
         |    "name": {
         |      "type": <error>"invalid"</error>
         |    }
         |  }
         |}
       """.stripMargin
    )
  }
}
