package org.psliwa.idea.composerJson.inspection

class PackageVersionInspectionTest extends InspectionTest {

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
}
