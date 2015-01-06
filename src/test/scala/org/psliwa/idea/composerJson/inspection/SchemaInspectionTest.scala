package org.psliwa.idea.composerJson.inspection

import org.junit.ComparisonFailure

class SchemaInspectionTest extends InspectionTest {

  private val RequiredProperties =
    """
      |"name": "vendor/pkg",
    """.stripMargin

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[SchemaInspection])
  }

  def testReportNotAllowedPropertyOnTopLevel() = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  <error descr="The 'unsupported' property is not allowed here.">"unsupported": "value"</error>
        |}
      """.stripMargin
    )
  }

  def testReportNotAllowedPropertyInNestedLevel() = {
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

  def testReportInvalidPropertyValueInEnum() = {

    val allowed = List("dev", "alpha", "beta", "RC", "stable").map("'"+_+"'").mkString(" or ")

    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "minimum-stability": <error descr="Value 'invalid' is not allowed here, it must be $allowed.">"invalid"</error>
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportValidPropertyValueInEnum() = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "type": "library"
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueInBooleanProperty() = {
    checkInspection(
      s"""
         |{
         |  $RequiredProperties
         |  "prefer-stable": <error descr="The value must be a boolean, string given.">"false"</error>
         |}
       """.stripMargin
    )
  }

  def testReportInvalidArrayPropertyValueInStringProperty() = {
    checkInspection(
      s"""
        |{
        |  "name": <error descr="The value must be a string, array given.">[ "some value" ]</error>,
        |  "description": "some desc"
        |}
      """.stripMargin
    )
  }

  def testReportInvalidObjectPropertyValueInStringProperty() = {
    checkInspection(
      s"""
        |{
        |  "name": <error descr="The value must be a string, object given.">{ "some name": "some value" }</error>,
        |  "description": "some desc"
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueInObjectProperty() = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "config": <error descr="The value must be an object, string given.">"value"</error>
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueTypeInEnumProperty() = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "type": <error descr="The value must be a string, object given.">{}</error>
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportErrorWhenObjectPropertyHasNotValueYet() = {
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
      case e: ComparisonFailure => //ignore syntax error assertionError, check only scala MatchFailure
    }
  }

  def testReportInvalidItemValueInArray() = {
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

  def testReportInvalidPropertyValueInArray() = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "authors": <error descr="The value must be an array, object given.">{}</error>
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueInOrSchema() = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "license": <error descr="The value must be a string or array, object given.">{ "some": "value" }</error>
        |}
      """.stripMargin
    )
  }

  def testReportInvalidPropertyValueInNumericProperty() = {
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

  def testDoesNotReportValidPropertyValueInNumericProperty() = {
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

  def testReportInvalidPropertyValueInPackages() = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "require": <error descr="The value must be an object, array given.">[]</error>
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportValidPropertyValueInPackages() = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "require": {}
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportValidPropertyValueInArray() = {
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

  def testDoesNotReportValidExtraProperty() = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "extra": {
        |    "prop1": "value1",
        |    "prop1": "value1"
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

  def testDoesNotReportValidRequireWithPackagesAndVersions() = {
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

  def testReportInvalidStringPropertyValue() = {
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

  def testReportMissingProperties() = {
    checkInspection(
      """
        |<error descr="The 'name' property is required.">{}</error>
      """.stripMargin
    )
  }

  def testReportMissingPropertiesOnDeeperLevel() = {
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

  def testDoesNotReportWhenStringPropertyValueIsEmptyString() = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "target-dir": ""
        |}
      """.stripMargin
    )
  }

  def testDoesNotReportAnyErrorsWhenFileIsEmpty() = {
    checkInspection("")
  }
}
