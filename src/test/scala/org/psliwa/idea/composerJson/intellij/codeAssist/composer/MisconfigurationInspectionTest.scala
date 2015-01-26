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
        |  "type": "project",
        |  "prefer-stable": false
        |}
      """.stripMargin
    )
  }
}
