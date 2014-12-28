package org.psliwa.idea.composerJson.inspection

import java.io.File

/**
 * Tests for inspections on few real-live composer.json files
 */
class ValidComposerJsonFilesInspectionTest extends InspectionTest {

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[SchemaInspection])
    myFixture.enableInspections(classOf[FilePathInspection])
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
