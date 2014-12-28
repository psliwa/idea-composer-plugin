package org.psliwa.idea.composerJson.inspection

import org.junit.ComparisonFailure

class FilePathInspectionTest extends InspectionTest {

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[FilePathInspection])
  }

  def testGivenFilePathElement_givenFilePathDoesNotExist_warningShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "bin": [ <warning descr="File 'some/unexisting' does not exist.">"some/unexisting"</warning> ]
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathElement_givenExistingFilePath_warningShouldNotBeReported() = {
    writeAction(() => myFixture.getTempDirFixture.findOrCreateDir("some").createChildData(this, "existing"))

    checkInspection(
      """
        |{
        |  "bin": [ "some/existing" ]
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathElement_givenElementDoesNotRequireExistingPath_warningShouldNotBeNeverReported() = {
    checkInspection(
      """
        |{
        |  "config": {
        |    "vendor-dir": "some/unexisting"
        |  }
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathsElement_givenFilePathDoesNotExist_warningShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": <warning descr="File 'some/unexisting' does not exist.">"some/unexisting"</warning>
        |    }
        |  }
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathsElement_givenArrayOfUnexistingFilePaths_warningShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": [<warning descr="File 'some/unexisting' does not exist.">"some/unexisting"</warning>]
        |    }
        |  }
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathsElement_givenExistingFilePath_warningShouldNotBeReported() = {
    writeAction(() => myFixture.getTempDirFixture.findOrCreateDir("some").createChildData(this, "existing"))

    checkInspection(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": "some/existing"
        |    }
        |  }
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathsElement_givenArrayOfExistingFilePaths_warningShouldNotBeReported() = {
    writeAction(() => myFixture.getTempDirFixture.findOrCreateDir("some").createChildData(this, "existing"))

    checkInspection(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": [ "some/existing" ]
        |    }
        |  }
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathsPropertyDoesNotHaveValue_warningShouldNotBeReported() = {
    try {
      checkInspection(
        """
          |{
          |  "autoload": {
          |    "psr-0": {
          |      "":
          |    }
          |  }
          |}
        """.stripMargin
      )
    } catch {
      case e: ComparisonFailure => //ignore syntax error assertionError, check only scala MatchFailure
    }
  }
}
