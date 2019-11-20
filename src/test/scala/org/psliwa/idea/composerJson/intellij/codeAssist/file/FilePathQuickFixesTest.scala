package org.psliwa.idea.composerJson.intellij.codeAssist.file

import org.junit.Assert._
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest

class FilePathQuickFixesTest extends InspectionTest {

  private val RemoveEntryQuickFix = ComposerBundle.message("inspection.quickfix.removeEntry")
  private val CreateDirectoryQuickFix = ComposerBundle.message("inspection.quickfix.createDirectory", _: String)
  private val CreateFileQuickFix = ComposerBundle.message("inspection.quickfix.createFile", _: String)

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[FilePathInspection])
  }

  def testRunRemoveValueQuickFix_valueShouldBeRemoved(): Unit = {
    checkQuickFix(RemoveEntryQuickFix)(
      """
        |{
        |  "bin": [
        |    "some/unexisting"
        |  ]
        |}
      """.stripMargin,
      """
        |{
        |  "bin": [
        |  ]
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathIsLastOne_runRemoveValueQuickFix_lastCommaShouldBeRemoved(): Unit = {
    writeAction(() => myFixture.getTempDirFixture.findOrCreateDir("some").createChildData(this, "existing"))

    checkQuickFix(RemoveEntryQuickFix)(
      """
        |{
        |  "bin": [
        |    "some/existing",
        |    "some/unexisting"
        |  ]
        |}
      """.stripMargin,
      """
        |{
        |  "bin": [
        |    "some/existing"
        |  ]
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathsElement_runRemovePropertyQuickFix_propertyShouldBeRemoved(): Unit = {
    checkQuickFix(RemoveEntryQuickFix)(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": "some/unexisting"
        |    }
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |    }
        |  }
        |}
      """.stripMargin
    )
  }

  def testGivenFilePathsElement_filePathsGivenAsArray_runRemovePropertyQuickFix_propertyShouldBeRemoved(): Unit = {
    checkQuickFix(RemoveEntryQuickFix)(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": ["some/unexisting"]
        |    }
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": []
        |    }
        |  }
        |}
      """.stripMargin
    )
  }

  def testRunCreateDirectoryQuickFix_directoryShouldBeCreated(): Unit = {
    val dir = "some/unexisting"

    runQuickFix(CreateDirectoryQuickFix(dir))(
      s"""
        |{
        |  "bin": [ "$dir" ]
        |}
      """.stripMargin
    )

    assertTrue(findDir(dir).isDefined)
  }

  def testGivenFilePathPartiallyExists_runCreateDirectoryQuickFix_missingDirectoriesShouldBeCreated(): Unit = {
    val dir = "some/unexisting"

    writeAction(() => myFixture.getTempDirFixture.findOrCreateDir(dir.split("/")(0)))

    runQuickFix(CreateDirectoryQuickFix(dir))(
      s"""
        |{
        |  "bin": [ "$dir" ]
        |}
      """.stripMargin
    )

    assertTrue(findDir(dir).isDefined)
  }

  def testGivenFilePathHasTwoSlashesInRow_directoryShouldBeCreated(): Unit = {
    val dir = "some//unexisting"

    runQuickFix(CreateDirectoryQuickFix(dir))(
      s"""
        |{
        |  "bin": [ "$dir" ]
        |}
      """.stripMargin
    )

    assertTrue(findDir(dir).isDefined)
  }

  def testDirectoryInGivenPathCannotBeCreate_directoryShouldNotBeCreated(): Unit = {
    val dir = "some/unexisting"
    writeAction(() => myFixture.getTempDirFixture.createFile(dir.split("/")(0)))

    runQuickFix(CreateDirectoryQuickFix(dir))(
      s"""
        |{
        |  "bin": [ "$dir" ]
        |}
      """.stripMargin
    )

    assertTrue(findDir(dir).isEmpty)
  }

  def testRunCreateFileQuickFix_fileShouldBeCreated(): Unit = {
    val dir = "some/unexisting.file"

    runQuickFix(CreateFileQuickFix(dir))(
      s"""
        |{
        |  "bin": [ "$dir" ]
        |}
      """.stripMargin
    )

    assertTrue(findFile(dir).isDefined)
  }

  def testRunCreateFileQuickFix_filePathIsRelative_fileShouldBeCreated(): Unit = {
    val dir = "./some/../some/unexisting.file"

    runQuickFix(CreateFileQuickFix(dir))(
      s"""
         |{
         |  "bin": [ "$dir" ]
         |}
      """.stripMargin
    )

    assertTrue(findFile(dir).isDefined)
  }

  private def findDir = {
    import org.psliwa.idea.composerJson.util.Files

    Files.findDir(myFixture.getFile.getContainingDirectory, _: String)
  }

  private def findFile = {
    import org.psliwa.idea.composerJson.util.Files

    Files.findFile(myFixture.getFile.getContainingDirectory, _: String)
  }
}
