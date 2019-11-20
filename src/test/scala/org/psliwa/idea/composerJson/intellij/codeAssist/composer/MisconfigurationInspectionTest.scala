package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest

class MisconfigurationInspectionTest extends InspectionTest {
  val RequiredProperties = """"license":"proprietary","""

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[MisconfigurationInspection])
  }

  def testUnstableProject_givenProjectType_givenDevMinimumStability_givenDisabledPreferStable_misconfigurationShouldBeReported()
      : Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "name": "some/pkg",
        |  <weak_warning>"type": "project"</weak_warning>,
        |  <weak_warning>"minimum-stability": "dev"</weak_warning>
        |}
      """.stripMargin
    )
  }

  def testUnstableProject_givenProjectType_givenDevMinimumStability_givenExplicitlyDisabledPreferStable_misconfigurationShouldBeReported()
      : Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "name": "some/pkg",
        |  <weak_warning>"type": "project"</weak_warning>,
        |  <weak_warning>"minimum-stability": "dev"</weak_warning>,
        |  <weak_warning>"prefer-stable": false</weak_warning>
        |}
      """.stripMargin
    )
  }

  def testUnstableProject_givenProjectType_givenDevMinimumStability_givenEnabledPreferStable_misconfigurationShouldNotBeReported()
      : Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "name": "some/pkg",
        |  "type": "project",
        |  "minimum-stability": "dev",
        |  "prefer-stable": true
        |}
      """.stripMargin
    )
  }

  def testUnstableProject_givenLibraryType_givenDevMinimumStability_givenDisabledPreferStable_misconfigurationShouldNotBeReported()
      : Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "name": "some/pkg",
        |  "description": "desc",
        |  "type": "library",
        |  "minimum-stability": "dev",
        |  "prefer-stable": false
        |}
      """.stripMargin
    )
  }

  def testUnstableProject_givenProjectType_givenStableMinimumStability_givenDisabledPreferStable_misconfigurationShouldNotBeReported()
      : Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "name": "some/pkg",
        |  "type": "project",
        |  "minimum-stability": "stable"
        |}
      """.stripMargin
    )
  }

  def testUnstableProject_givenProjectType_givenMissingMinimumStability_givenDisabledPreferStable_misconfigurationShouldNotBeReported()
      : Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "name": "some/pkg",
        |  "type": "project",
        |  "prefer-stable": false
        |}
      """.stripMargin
    )
  }

  def testLicenseProperty_itIsMissing_warningShouldBeReported(): Unit = {
    checkInspection(
      s"""
        |{
        |  <weak_warning>"name": "aaa/bbb"</weak_warning>
        |}
      """.stripMargin
    )
  }

  def testLicenseProperty_itIsDefinedAsArray_warningShouldNotBeReported(): Unit = {
    checkInspection(
      s"""
         |{
         |  "name": "aaa/bbb",
         |  "license": ["proprietary"]
         |}
      """.stripMargin
    )
  }

  def testTypeProperty_typeIsComposerInstaller_warningShouldBeReported(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "name": "some/name",
        |  "description": "desc",
        |  <weak_warning>"type": "composer-installer"</weak_warning>
        |}
      """.stripMargin
    )
  }

  def testNameIsCamelCase_warningShouldBeReported(): Unit = {
    checkInspection(
      s"""
         |{
         |  $RequiredProperties
         |  <weak_warning>"name": "someVendor/somePackage"</weak_warning>
         |}
       """.stripMargin
    )
  }

  def testDependencyDefinedInRequireAndRequireDev_warningShouldBeReported(): Unit = {
    checkInspection(
      s"""
         |{
         |  $RequiredProperties
         |  "require": {
         |    <weak_warning>"some/pkg": "1.0.0"</weak_warning>
         |  },
         |  "require-dev": {
         |    <weak_warning>"some/pkg": "1.0.1"</weak_warning>
         |  }
         |}
       """.stripMargin
    )
  }

  def testEmptyNamespaceForPsr0_warningShouldBeReported(): Unit = {
    checkInspection(
      s"""
         |{
         |  $RequiredProperties
         |  "autoload": {
         |    "psr-0": {
         |      <weak_warning>"": "abc"</weak_warning>,
         |      "Def": "def"
         |    },
         |    "psr-4": {
         |      <weak_warning>"": "abc"</weak_warning>,
         |      "Def": "def"
         |    }
         |  }
         |}
       """.stripMargin
    )
  }

  def testCommitRefAsPackageVersion_warningShouldBeReported(): Unit = {
    checkInspection(
      s"""
         |{
         |  $RequiredProperties
         |  "require": {
         |    <weak_warning>"some/pkg1": "dev-master#adcdf"</weak_warning>,
         |    "some/pkg2": "1.0.0"
         |  }
         |}
       """.stripMargin
    )
  }

  def testNameProperty_givenLibraryPackageType_namePropertyIsMissing_errorShouldBeReported(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "description": "desc",
        |  <error>"type": "library"</error>
        |}
      """.stripMargin
    )
  }

  def testDescriptionProperty_givenLibraryPackageType_descPropertyIsMissing_errorShouldBeReported(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "name": "some/pkg",
        |  <error>"type": "library"</error>
        |}
      """.stripMargin
    )
  }

  def testNameProperty_givenLibraryPackageType_givenNameProperty_errorShouldNotBeReported(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "name": "some/pkg",
        |  "description": "desc",
        |  "type": "library"
        |}
      """.stripMargin
    )
  }

  def testNameProperty_givenProjectPackageType_namePropertyIsMissing_errorShouldNotBeReported(): Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  "type": "project"
        |}
      """.stripMargin
    )
  }

  def testNameProperty_givenLibraryPackageType_namePropertyIsMissing_givenAuthorsProperty_errorShouldBeReported()
      : Unit = {
    checkInspection(
      s"""
        |{
        |  $RequiredProperties
        |  <error>"type": "library"</error>,
        |  "authors": [
        |     {
        |       "name": "psliwa"
        |     }
        |  ]
        |}
      """.stripMargin
    )
  }
}
