package org.psliwa.idea.composerJson.inspection

import org.psliwa.idea.composerJson.ComposerBundle

class SchemaQuickFixesTest extends InspectionTest {

  private val RequiredProperties =
    """
      |"name": "vendor/pkg",
    """.stripMargin

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[SchemaInspection])
  }

  private val RemoveEntryQuickFix = ComposerBundle.message("inspection.quickfix.removeEntry")
  private val RemoveQuotesQuickFix = ComposerBundle.message("inspection.quickfix.removeQuotes")
  private val CreatePropertyQuickFix = ComposerBundle.message("inspection.quickfix.createProperty", _: String)

  def testRemoveQuotes_givenQuotedNumber_quotesShouldBeRemoved() = {
    checkQuickFix(RemoveQuotesQuickFix)(
      s"""
        |{
        |  $RequiredProperties
        |  "config": {
        |    "cache-files-ttl": "123"
        |  }
        |}
      """.stripMargin,
      s"""
        |{
        |  $RequiredProperties
        |  "config": {
        |    "cache-files-ttl": 123
        |  }
        |}
      """.stripMargin
    )
  }

  def testRemoveQuotes_givenQuotedString_quotesShouldNotBeRemoved() = {
    checkQuickFix(RemoveQuotesQuickFix, 0)(
      s"""
        |{
        |  $RequiredProperties
        |  "config": {
        |    "cache-files-ttl": "some invalid"
        |  }
        |}
      """.stripMargin,
      s"""
        |{
        |  $RequiredProperties
        |  "config": {
        |    "cache-files-ttl": "some invalid"
        |  }
        |}
      """.stripMargin
    )
  }

  def testRemoveEntry_givenUnsupportedProperty_itShouldBeRemoved() = {
    checkQuickFix(RemoveEntryQuickFix)(
      s"""
        |{
        |  $RequiredProperties
        |  "require": {},
        |  "authors": [
        |    {
        |      "unsupported": "value",
        |      "name": "psliwa"
        |    }
        |  ]
        |}
      """.stripMargin,
      s"""
        |{
        |  $RequiredProperties
        |  "require": {},
        |  "authors": [
        |    {
        |      "name": "psliwa"
        |    }
        |  ]
        |}
      """.stripMargin
    )
  }

  def testRemoveEntry_givenUnsupportedPropertyIsLastOne_commaAfterPreviousPropertyShouldBeRemoved() = {
    checkQuickFix(RemoveEntryQuickFix)(
      s"""
        |{
        |  $RequiredProperties
        |  "require": {},
        |  "unsupported": {
        |    "prop": "value"
        |  }
        |}
      """.stripMargin,
      s"""
        |{
        |  $RequiredProperties
        |  "require": {}
        |}
      """.stripMargin
    )
  }

  def testRemoveEntry_givenObjectProperty_givenPropertyIsNotLastOne_commaAfterPreviousPropertyShouldNotBeRemoved() = {
    checkQuickFix(RemoveEntryQuickFix)(
      s"""
        |{
        |  $RequiredProperties
        |  "require": {},
        |  "unsupported": {
        |    "prop": "value"
        |  },
        |  "require-dev": {}
        |}
      """.stripMargin,
      s"""
        |{
        |  $RequiredProperties
        |  "require": {},
        |  "require-dev": {}
        |}
      """.stripMargin
    )
  }

  def testRemoveProperty_givenStringProperty_givenPropertyIsNotLastOne_commaAfterPreviousPropertyShouldNotBeRemoved() = {
    checkQuickFix(RemoveEntryQuickFix)(
      s"""
        |{
        |  $RequiredProperties
        |  "require": {},
        |  "unsupported": "value",
        |  "require-dev": {}
        |}
      """.stripMargin,
      s"""
        |{
        |  $RequiredProperties
        |  "require": {},
        |  "require-dev": {}
        |}
      """.stripMargin
    )
  }

  //  TODO: Quick fix is not shown on top level, why?
  //  def testQuickFixForQuotedBooleans() = {
  //    checkQuickFix(
  //      s"""
  //        |{
  //        |  $RequiredProperties
  //        |  "prefer-stable": "true"
  //        |}
  //      """.stripMargin,
  //      s"""
  //        |{
  //        |  $RequiredProperties
  //        |  "prefer-stable": true
  //        |}
  //      """.stripMargin
  //    )
  //  }

  def testCreateProperty_propertyShouldBeCreated() = {
    checkQuickFix(CreatePropertyQuickFix("name"))(
      s"""
        |{<caret>
        |}
      """.stripMargin,
      s"""
        |{
        |  "name": "<caret>"
        |}
      """.stripMargin
    )
  }

  def testCreateProperty_thereAreSomePropertiesAlready_caretIsBeforeExistingProperty_propertyShouldBeCreatedBeforeExistingOne() = {
    checkQuickFix(CreatePropertyQuickFix("name"))(
      s"""
        |{<caret>
        |  "require": {}
        |}
      """.stripMargin,
      s"""
        |{
        |  "name": "<caret>",
        |  "require": {}
        |}
      """.stripMargin
    )
  }

  def testCreateProperty_thereAreSomePropertiesAlready_caretIsAfterExistingProperty_propertyShouldBeCreatedAfterExistingOne() = {
    checkQuickFix(CreatePropertyQuickFix("name"))(
      s"""
        |{
        |  "require": {}<caret>
        |}
      """.stripMargin,
      s"""
        |{
        |  "require": {},
        |  "name": "<caret>"
        |}
      """.stripMargin
    )
  }

  def testCreateProperty_thereAreSomePropertiesAlready_caretIsBetweenExistingProperties_propertyShouldBeCreatedBetweenExistingOne() = {
    checkQuickFix(CreatePropertyQuickFix("name"))(
      s"""
        |{
        |  "require": {},<caret>
        |  "require-dev": {}
        |}
      """.stripMargin,
      s"""
        |{
        |  "require": {},
        |  "name": "<caret>",
        |  "require-dev": {}
        |}
      """.stripMargin
    )
  }

  def testCreateProperty_thereAreSomePropertiesAlready_caretIsInsideFirstProperty_propertyShouldBeCreatedAfterThatProperty() = {
    checkQuickFix(CreatePropertyQuickFix("name"))(
      s"""
        |{
        |  "require<caret>": {},
        |  "require-dev": {}
        |}
      """.stripMargin,
      s"""
        |{
        |  "require": {},
        |  "name": "<caret>",
        |  "require-dev": {}
        |}
      """.stripMargin
    )
  }

  def testCreateProperty_noneNewLineGiven_newLinesShouldBeFixed() = {
    checkQuickFix(CreatePropertyQuickFix("name"))(
      s"""
        |{<caret>}
      """.stripMargin,
      s"""
        |{
        |  "name": "<caret>"
        |}
      """.stripMargin
    )
  }
}
