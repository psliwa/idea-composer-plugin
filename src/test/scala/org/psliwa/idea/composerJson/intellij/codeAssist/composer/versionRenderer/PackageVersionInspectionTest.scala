package org.psliwa.idea.composerJson.intellij.codeAssist.composer.versionRenderer

import com.intellij.openapi.application.ApplicationManager
import org.psliwa.idea.composerJson.fixtures.ComposerFixtures._
import org.psliwa.idea.composerJson.composer.ComposerPackages
import org.psliwa.idea.composerJson.composer.ComposerPackage
import org.junit.Assert._
import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest

class PackageVersionInspectionTest extends InspectionTest {

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[PackageVersionInspection])

    val overlay: VersionOverlay = getOverlay
    overlay.clearPackageVersions()
  }

  private def getOverlay: VersionOverlay = {
    val app = ApplicationManager.getApplication
    val overlay = app.getComponent(classOf[VersionOverlay])
    overlay
  }

  def testGivenInstalledPackage_itsVersionShouldBeCollected(): Unit = {
    createComposerLock(myFixture, ComposerPackages(ComposerPackage("some/pkg", "1.0.1")), ".")

    checkInspection(
      s"""
        |{
        |  "require": {
        |    <caret>"some/pkg": "1.0.0"
        |  }
        |}
      """.stripMargin
    )

    assertPackageVersions(List(PackageVersion(myFixture.getCaretOffset, "1.0.1")))
  }

  private def assertPackageVersions(expected: List[PackageVersion]): Unit = {
    assertEquals(
      expected,
      getOverlay.getPackageVersions(myFixture.getFile.getVirtualFile.getCanonicalPath)
    )
  }

  override def isWriteActionRequired: Boolean = false
}
