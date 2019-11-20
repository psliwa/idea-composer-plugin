package org.psliwa.idea.composerJson.intellij.codeAssist.composer.infoRenderer

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.vfs.VirtualFile
import org.junit.Assert._
import org.psliwa.idea.composerJson.composer.model.PackageDescriptor
import org.psliwa.idea.composerJson.fixtures.ComposerFixtures
import org.psliwa.idea.composerJson.fixtures.ComposerFixtures._
import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest

class PackageInfoInspectionTest extends InspectionTest {

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[PackageInfoInspection])

    val overlay: PackageInfoOverlay = getOverlay
    overlay.clearPackagesInfo()
  }

  private def getOverlay: PackageInfoOverlay = {
    val app = ApplicationManager.getApplication
    val overlay = app.getComponent(classOf[PackageInfoOverlay])
    overlay
  }

  def testGivenInstalledPackage_itsVersionShouldBeCollected(): Unit = {
    createComposerLock(List(PackageDescriptor("some/pkg", "1.0.1")))

    checkInspection(
      s"""
        |{
        |  "require": {
        |    <caret>"some/pkg": "1.0.0"
        |  }
        |}
      """.stripMargin
    )

    assertPackageVersions(List(PackageInfo(myFixture.getCaretOffset, "1.0.1")))
  }

  private def createComposerLock(packages: List[PackageDescriptor], dir: String = "."): VirtualFile = {
    ComposerFixtures.createComposerLock(myFixture, packages.map(ComposerPackageWithReplaces(_, Set.empty)), dir)
  }

  private def assertPackageVersions(expected: List[PackageInfo]): Unit = {
    assertEquals(
      expected,
      getOverlay.getPackagesInfo(myFixture.getFile.getVirtualFile.getCanonicalPath)
    )
  }

  override def isWriteActionRequired: Boolean = false
}
