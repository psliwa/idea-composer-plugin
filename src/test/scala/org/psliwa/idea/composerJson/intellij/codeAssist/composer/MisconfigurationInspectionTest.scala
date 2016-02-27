package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest

class MisconfigurationInspectionTest extends InspectionTest {
  val RequiredProperties = """"license":"proprietary","""

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[MisconfigurationInspection])
  }

  def testUnstableProject_givenProjectType_givenDevMinimumStability_givenDisabledPreferStable_misconfigurationShouldBeReported() = {
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

  def testUnstableProject_givenProjectType_givenDevMinimumStability_givenExplicitlyDisabledPreferStable_misconfigurationShouldBeReported() = {
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

  def testUnstableProject_givenProjectType_givenDevMinimumStability_givenEnabledPreferStable_misconfigurationShouldNotBeReported() = {
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

  def testUnstableProject_givenLibraryType_givenDevMinimumStability_givenDisabledPreferStable_misconfigurationShouldNotBeReported() = {
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

  def testUnstableProject_givenProjectType_givenStableMinimumStability_givenDisabledPreferStable_misconfigurationShouldNotBeReported() = {
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

  def testUnstableProject_givenProjectType_givenMissingMinimumStability_givenDisabledPreferStable_misconfigurationShouldNotBeReported() = {
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

  def testLicenseProperty_itIsMissing_warningShouldBeReported() = {
    checkInspection(
      s"""
        |{
        |  <weak_warning>"name": "aaa/bbb"</weak_warning>
        |}
      """.stripMargin
    )
  }


  def testTypeProperty_typeIsComposerInstaller_warningShouldBeReported() = {
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

  def testNameIsCamelCase_warningShouldBeReported() = {
    checkInspection(
      s"""
         |{
         |  $RequiredProperties
         |  <weak_warning>"name": "someVendor/somePackage"</weak_warning>
         |}
       """.stripMargin
    )
  }

  def testDependencyDefinedInRequireAndRequireDev_warningShouldBeReported() = {
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

  def testEmptyNamespaceForPsr0_warningShouldBeReported() = {
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

  def testCommitRefAsPackageVersion_warningShouldBeReported() = {
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
}
