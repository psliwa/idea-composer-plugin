package org.psliwa.idea.composerJson.composer

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.{VfsUtil, VirtualFile}
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.junit.Assert._
import org.psliwa.idea.composerJson

class InstalledPackagesTest extends LightPlatformCodeInsightFixtureTestCase {

  var composerJsonFile: VirtualFile = _

  override def setUp() = {
    super.setUp()
    composerJsonFile = createComposerJson()
  }

  def testComposerLockNotExist_installedPackagesShouldBeEmpty() = {
    assertTrue(installedPackages().isEmpty)
  }

  private def installedPackages(file: VirtualFile = composerJsonFile) = InstalledPackages.forFile(file)

  def testEmptyComposerLockExist_installedPackagesShouldBeEmpty() = {
    createComposerLock(Packages())

    assertTrue(installedPackages().isEmpty)
  }

  def testGivenComposerLockWithFewPackages_installedPackagesShouldBeTheSame() = {
    val packages = Packages(Package("vendor/name", "1.0.0"), Package("vendor2/name2", "2.0.0"))
    createComposerLock(packages)

    assertEquals(packages, installedPackages())
  }

  def testGivenTwoComposerLockInDifferentLocations_installedPackagesDependOnRequestedFile() = {
    val subComposerJson = createComposerJson("subdir")

    val packages1 = Packages(Package("vendor/name", "1.0.0"), Package("vendor2/name2", "2.0.0"))
    val packages2 = Packages(Package("vendor3/name3", "1.0.0"), Package("vendor24/name24", "2.0.0"))

    createComposerLock(packages1)
    createComposerLock(packages2, "subdir")

    assertEquals(packages1, installedPackages())
    assertEquals(packages2, installedPackages(subComposerJson))
  }

  def testGivenComposerLockWithFewPackages_deleteComposerLock_installedPackagesShouldBeEmpty() = {
    val packages = Packages(Package("vendor/name", "1.0.0"))
    val file = createComposerLock(packages)

    assertEquals(packages, installedPackages())

    writeAction(() => file.delete(this))

    assertTrue(installedPackages().isEmpty)
  }

  def testGivenComposerLockWithFewPackages_moveIt_installedPackagesShouldBeEmpty() = {
    val packages = Packages(Package("vendor/name", "1.0.0"))
    val file = createComposerLock(packages)

    writeAction(() => file.move(this, myFixture.getTempDirFixture.findOrCreateDir("subdir")))

    assertTrue(installedPackages().isEmpty)
  }

  def testGivenComposerLockWithFewPackages_moveIt_givenComposerJsonInMoveDest_installedPackagesShouldBeTheSame() = {
    val packages = Packages(Package("vendor/name", "1.0.0"))
    val file = createComposerLock(packages)

    writeAction(() => file.move(this, myFixture.getTempDirFixture.findOrCreateDir("subdir")))
    val composerJson = writeAction(() => createComposerJson("subdir"))

    assertEquals(packages, installedPackages(composerJson))
  }

  private def createComposerLock(packages: Packages, dir: String = ".") = {
    val packagesJson = packages.map{ case(name,version) =>
      s"""{
        |  "name": "$name",
        |  "version": "$version"
        |}
      """.stripMargin
    }.mkString(",\n")

    val file = writeAction(() => myFixture.getTempDirFixture.findOrCreateDir(dir).createChildData(this, composerJson.ComposerLock))
    saveText(file, s"""
        |{
        |  "packages": [ $packagesJson ]
        |}
      """.stripMargin
    )

    file
  }

  private def createComposerJson(dir: String = ".") = {
    val file = writeAction(() => myFixture.getTempDirFixture.findOrCreateDir(dir).createChildData(this, composerJson.ComposerJson))
    saveText(file, "{}")

    file
  }

  private def saveText(file: VirtualFile, text: String) = {
    writeAction(() => VfsUtil.saveText(file, text))
  }

  private def writeAction[A](f: () => A): A = {
    ApplicationManager.getApplication.runWriteAction(new Computable[A] {
      override def compute = f()
    })
  }
}
