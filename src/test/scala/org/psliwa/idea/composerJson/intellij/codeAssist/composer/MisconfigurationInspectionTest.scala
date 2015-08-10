package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest

class MisconfigurationInspectionTest extends InspectionTest {
  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[MisconfigurationInspection])
  }

  def testUnstableProject_givenProjectType_givenDevMinimumStability_givenDisabledPreferStable_misconfigurationShouldBeReported() = {
    checkInspection(
      s"""
        |{
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
        |  "name": "some/pkg",
        |  "type": "project",
        |  "prefer-stable": false
        |}
      """.stripMargin
    )
  }

  def testNameProperty_givenLibraryPackageType_namePropertyIsMissing_errorShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "description": "desc",
        |  <error>"type": "library"</error>
        |}
      """.stripMargin
    )
  }

  def testDescriptionProperty_givenLibraryPackageType_descPropertyIsMissing_errorShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "name": "some/pkg",
        |  <error>"type": "library"</error>
        |}
      """.stripMargin
    )
  }

  def testNameProperty_givenLibraryPackageType_givenNameProperty_errorShouldNotBeReported() = {
    checkInspection(
      """
        |{
        |  "name": "some/pkg",
        |  "description": "desc",
        |  "type": "library"
        |}
      """.stripMargin
    )
  }

  def testNameProperty_givenProjectPackageType_namePropertyIsMissing_errorShouldNotBeReported() = {
    checkInspection(
      """
        |{
        |  "type": "project"
        |}
      """.stripMargin
    )
  }

  def testNameProperty_givenLibraryPackageType_namePropertyIsMissing_givenAuthorsProperty_errorShouldBeReported() = {
    checkInspection(
      """
        |{
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
