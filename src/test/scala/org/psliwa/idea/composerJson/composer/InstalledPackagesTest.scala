package org.psliwa.idea.composerJson.composer

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert._
import org.psliwa.idea.composerJson.composer.model.{PackageDescriptor, Packages}
import org.psliwa.idea.composerJson.fixtures.ComposerFixtures
import org.psliwa.idea.composerJson.fixtures.ComposerFixtures._

class InstalledPackagesTest extends BasePlatformTestCase {

  var composerJsonFile: VirtualFile = _

  override def setUp(): Unit = {
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
    val packages = Packages(PackageDescriptor("vendor/name", "1.0.0"), PackageDescriptor("vendor2/name2", "2.0.0"))
    createComposerLock(packages)

    assertEquals(packages, installedPackages())
  }

  def testGivenComposerLockPackageWithReplacesProperty_installedPackagesShouldBeReplacedPackagesAsWell(): Unit = {
    val packages = List(ComposerPackageWithReplaces(PackageDescriptor("vendor/name", "1.0.0"), Set("replaced/name")))
    ComposerFixtures.createComposerLock(myFixture, packages)

    val originalPackage = PackageDescriptor("vendor/name", "1.0.0")
    val replacedPackage = PackageDescriptor("replaced/name", "1.0.0", replacedBy = Some(originalPackage))
    val expectedPackages = Packages(originalPackage, replacedPackage)

    assertEquals(expectedPackages, installedPackages())
  }

  def testGivenTwoComposerLockInDifferentLocations_installedPackagesDependOnRequestedFile() = {
    val subComposerJson = createComposerJson("subdir")

    val packages1 = Packages(PackageDescriptor("vendor/name", "1.0.0"), PackageDescriptor("vendor2/name2", "2.0.0"))
    val packages2 = Packages(PackageDescriptor("vendor3/name3", "1.0.0"), PackageDescriptor("vendor24/name24", "2.0.0"))

    createComposerLock(packages1)
    createComposerLock(packages2, "subdir")

    assertEquals(packages1, installedPackages())
    assertEquals(packages2, installedPackages(subComposerJson))
  }

  def testGivenComposerLockWithFewPackages_deleteComposerLock_installedPackagesShouldBeEmpty() = {
    val packages = Packages(PackageDescriptor("vendor/name", "1.0.0"))
    val file = createComposerLock(packages)

    assertEquals(packages, installedPackages())

    writeAction(() => file.delete(this))

    assertTrue(installedPackages().isEmpty)
  }

  def testGivenComposerLockWithFewPackages_moveIt_installedPackagesShouldBeEmpty() = {
    val packages = Packages(PackageDescriptor("vendor/name", "1.0.0"))
    val file = createComposerLock(packages)

    writeAction(() => file.move(this, myFixture.getTempDirFixture.findOrCreateDir("subdir")))

    assertTrue(installedPackages().isEmpty)
  }

  def testGivenComposerLockWithFewPackages_moveIt_givenComposerJsonInMoveDest_installedPackagesShouldBeTheSame()
      : Unit = {
    val packages = Packages(PackageDescriptor("vendor/name", "1.0.0"))
    val file = createComposerLock(packages)

    writeAction(() => file.move(this, myFixture.getTempDirFixture.findOrCreateDir("subdir")))
    val composerJson = writeAction(() => createComposerJson("subdir"))

    assertEquals(packages, installedPackages(composerJson))
  }

  private def createComposerLock(packages: Packages, dir: String = "."): VirtualFile =
    ComposerFixtures.createComposerLock(myFixture,
                                        packages.descriptors.map(ComposerPackageWithReplaces(_, Set.empty)),
                                        dir)
  private def createComposerJson(dir: String = ".") = ComposerFixtures.createComposerJson(myFixture, dir)
}
