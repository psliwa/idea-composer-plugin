package org.psliwa.idea.composerJson.intellij.codeAssist.file

import org.junit.ComparisonFailure
import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest
import org.psliwa.idea.composerJson.ComposerJson

class FilePathInspectionTest extends InspectionTest {

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[FilePathInspection])
  }

  def testGivenFilePathElementInArray_givenFilePathDoesNotExist_warningShouldBeReported(): Unit = {
    checkInspection(
      """
        |{
        |  "bin": [ <warning descr="File 'some/unexisting' does not exist.">"some/unexisting"</warning> ]
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathElement_givenFilePathDoesNotExist_warningShouldBeReported(): Unit = {
    checkInspection(
      """
        |{
        |  "bin": <warning descr="File 'some/unexisting' does not exist.">"some/unexisting"</warning>
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathElementInArray_givenExistingFilePath_warningShouldNotBeReported(): Unit = {
    writeAction(() => myFixture.getTempDirFixture.findOrCreateDir("some").createChildData(this, "existing"))

    checkInspection(
      """
        |{
        |  "bin": [ "some/existing" ]
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathElement_givenExistingFilePath_warningShouldNotBeReported(): Unit = {
    writeAction(() => myFixture.getTempDirFixture.findOrCreateDir("some").createChildData(this, "existing"))

    checkInspection(
      """
        |{
        |  "bin": "some/existing"
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathElement_givenElementDoesNotRequireExistingPath_warningShouldNotBeNeverReported(): Unit = {
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

  def testGivenFilePathsElement_givenFilePathDoesNotExist_warningShouldBeReported(): Unit = {
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

  def testGivenFilePathsElement_givenArrayOfUnexistingFilePaths_warningShouldBeReported(): Unit = {
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

  def testGivenFilePathsElement_givenExistingFilePath_warningShouldNotBeReported(): Unit = {
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

  def testGivenFilePathsElement_givenExistingRelativeFilePath_warningShouldNotBeReported(): Unit = {
    writeAction(() => myFixture.getTempDirFixture.findOrCreateDir("some").createChildDirectory(this, "existing"))

    checkInspection(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": "./some/../some/existing/./../../some/existing"
        |    }
        |  }
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathsElement_givenRelativeUnexistingPath_warningShouldBeReported(): Unit = {
    checkInspection(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": <warning>"../../../../../../../../../../../../../../../../../../../../.."</warning>
        |    }
        |  }
        |}
      """.stripMargin
    )
  }

  //issue #10
  def testGivenComposerProjectInNestedDirectory_givenExistingRelativeFilePath_warningShouldNotBeReported(): Unit = {

    writeAction(() => findOrCreateDir("main"))
    writeAction(() => findOrCreateDir("tests").createChildData(this, "src"))

    checkInspection(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": "../tests/src"
        |    }
        |  }
        |}
      """.stripMargin,
      s"main/$ComposerJson"
    )
  }

  def testGivenFilePathsElement_givenArrayOfExistingFilePaths_warningShouldNotBeReported(): Unit = {
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

  def testGivenFilePathsPropertyDoesNotHaveValue_warningShouldNotBeReported(): Unit = {
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
      case _: ComparisonFailure => //ignore syntax error assertionError, check only scala MatchFailure
    }
  }

  def testFilepathButAlsoAcceptsUrl_givenValidUrl_warningShouldNotBeReported(): Unit = {
    checkInspection(
      """
        |{
        |  "repositories": [
        |    {
        |      "type": "composer",
        |      "url": "https://composer.typo3.org/"
        |    }
        |  ]
        |}
      """.stripMargin
    )
  }
}
