package org.psliwa.idea.composerJson.inspection

import org.psliwa.idea.composerJson.settings.{PatternItem, ComposerJsonSettings}

class PackageVersionInspectionTest extends InspectionTest {

  override def setUp(): Unit = {
    super.setUp()

    ComposerJsonSettings(myFixture.getProject).getUnboundedVersionInspectionSettings.clear()
  }

  def testGivenUnboundVersion_thatShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "require": {
        |    <warning>"vendor/pkg": ">=2.1.0"</warning>
        |  }
        |}
      """.stripMargin)
  }

  def testGivenBoundVersion_thatIsOk() = {
    checkInspection(
      """
        |{
        |  "require": {
        |    "vendor/pkg": "2.1.0"
        |  }
        |}
      """.stripMargin)
  }

  def testGivenUnboundVersion_givenPackageIsExcluded_thatIsOk() = {
    val pkg = "vendor/pkg"

    ComposerJsonSettings(myFixture.getProject).getUnboundedVersionInspectionSettings.addExcludedPattern(new PatternItem(pkg))

    checkInspection(
      s"""
        |{
        |  "require": {
        |    "$pkg": ">=2.1.0"
        |  }
        |}
      """.stripMargin)
  }
}
