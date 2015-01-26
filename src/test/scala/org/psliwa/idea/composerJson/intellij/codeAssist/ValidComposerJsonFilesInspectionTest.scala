package org.psliwa.idea.composerJson.intellij.codeAssist

import java.io.File

import org.psliwa.idea.composerJson.intellij.codeAssist.composer.MisconfigurationInspection
import org.psliwa.idea.composerJson.intellij.codeAssist.file.FilePathInspection
import org.psliwa.idea.composerJson.intellij.codeAssist.schema.SchemaInspection

/**
 * Tests for inspections on few real-live composer.json files
 */
class ValidComposerJsonFilesInspectionTest extends InspectionTest {

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[SchemaInspection])
    myFixture.enableInspections(classOf[FilePathInspection])
    myFixture.enableInspections(classOf[MisconfigurationInspection])
  }

  def testSymfonyComposerJson(): Unit = {
    checkComposerJson("symfony")
  }

  def testSymfonyStandardComposerJson(): Unit = {
    checkComposerJson("symfony_standard")
  }

  def testLaravelComposerJson(): Unit = {
    checkComposerJson("laravel")
  }

  def testDoctrineComposerJson(): Unit = {
    checkComposerJson("doctrine")
  }

  override def getTestDataPath: String = new File("src/test/resources/org/psliwa/idea/composerJson/inspection/").getAbsolutePath

  private def checkComposerJson(pkg: String): Unit = {
    myFixture.copyDirectoryToProject(pkg, "/")
    myFixture.testHighlighting(true, false, true, "/composer.json")
  }
}
