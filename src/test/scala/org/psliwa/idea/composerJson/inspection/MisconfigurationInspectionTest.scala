package org.psliwa.idea.composerJson.inspection

class MisconfigurationInspectionTest extends InspectionTest {
  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[MisconfigurationInspection])
  }

  def testGivenProjectType_givenDevMinimumStability_givenDisabledPreferStable_misconfigurationShouldBeReported() = {
    checkInspection(
      s"""
        |{
        |  "type": <weak_warning>"project"</weak_warning>,
        |  "minimum-stability": <weak_warning>"dev"</weak_warning>
        |}
      """.stripMargin
    )
  }

  def testGivenProjectType_givenDevMinimumStability_givenExplicitlyDisabledPreferStable_misconfigurationShouldBeReported() = {
    checkInspection(
      s"""
        |{
        |  "type": <weak_warning>"project"</weak_warning>,
        |  "minimum-stability": <weak_warning>"dev"</weak_warning>,
        |  "prefer-stable": <weak_warning>false</weak_warning>
        |}
      """.stripMargin
    )
  }

  def testGivenProjectType_givenDevMinimumStability_givenEnabledPreferStable_misconfigurationShouldNotBeReported() = {
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

  def testGivenLibraryType_givenDevMinimumStability_givenDisabledPreferStable_misconfigurationShouldNotBeReported() = {
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

  def testGivenProjectType_givenStableMinimumStability_givenDisabledPreferStable_misconfigurationShouldNotBeReported() = {
    checkInspection(
      s"""
        |{
        |  "type": "project",
        |  "minimum-stability": "stable"
        |}
      """.stripMargin
    )
  }

  def testGivenProjectType_givenMissingMinimumStability_givenDisabledPreferStable_misconfigurationShouldNotBeReported() = {
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
