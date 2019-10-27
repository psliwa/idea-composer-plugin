package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.openapi.vfs.VirtualFile
import org.psliwa.idea.composerJson.composer
import org.psliwa.idea.composerJson.fixtures.ComposerFixtures
import org.psliwa.idea.composerJson.fixtures.ComposerFixtures._
import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest

class NotInstalledPackageInspectionTest extends InspectionTest {

  override def setUp() = {
    super.setUp()

    myFixture.enableInspections(classOf[NotInstalledPackageInspection])
  }

  def testGivenUninstalledPackage_thatShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "require": {
        |    <weak_warning>"vendor/pkg": "1.0.2"</weak_warning>
        |  }
        |}
      """.stripMargin)
  }

  def testGivenVirtualPackage_thatShouldNotBeReported() = {
    checkInspection(
      """
        |{
        |  "require": {
        |    "php": ">=5.3"
        |  }
        |}
      """.stripMargin)
  }

  def testGivenInstalledPackage_thatShouldNotBeReported() = {
    createComposerLock(composer.ComposerPackages(composer.ComposerPackage("vendor/pkg", "1.0.2")))

    checkInspection(
      """
        |{
        |  "require": {
        |    "vendor/pkg": "1.0.2"
        |  }
        |}
      """.stripMargin)
  }

  private def createComposerLock(packages: composer.ComposerPackages): VirtualFile = {
    ComposerFixtures.createComposerLock(myFixture, packages.values.toList.map(ComposerPackageWithReplaces(_, Set.empty)))
  }

  def testGivenNotInstalledPackage_butReplacementIsInstalled_thatShouldNotBeReported(): Unit = {
    ComposerFixtures.createComposerLock(myFixture, List(ComposerPackageWithReplaces(
      composer.ComposerPackage("replacement/pkg", "1.0.2"), Set("vendor/pkg"))
    ))

    checkInspection(
      """
        |{
        |  "require": {
        |    "vendor/pkg": "1.0.2"
        |  }
        |}
      """.stripMargin)
  }

  def testGivenInstalledPackage_givenPackageNameInDifferentCase_thatShouldNotBeReported() = {
    createComposerLock(composer.ComposerPackages(composer.ComposerPackage("veNdor/pkg", "1.0.2")))

    checkInspection(
      """
        |{
        |  "require": {
        |    "Vendor/Pkg": "1.0.2"
        |  }
        |}
      """.stripMargin)
  }

  def testGivenUninstalledPackage_packageHasNotVersionYet_thatShouldNotBeReported() = {
    checkInspection(
      """
        |{
        |  "require": {
        |    "vendor/pkg": ""
        |  }
        |}
      """.stripMargin)
  }

  def testGivenInstalledProdPackage_thatPackageIsOnlyInRequireDev_thatShouldNotBeReported() = {
    createComposerLock(composer.ComposerPackages(composer.ComposerPackage("vendor/pkg", "1.0.2")))

    checkInspection(
      """
        |{
        |  "require-dev": {
        |    "vendor/pkg": "1.0.2"
        |  }
        |}
      """.stripMargin)
  }

  def testGivenUninstalledPackage_givenPackageIsInRequireDev_thatShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "require-dev": {
        |    <weak_warning>"vendor/pkg": "1.0.2"</weak_warning>
        |  }
        |}
      """.stripMargin)
  }

  def testGivenInstalledDevPackage_thatPackageIsOnlyInRequireDev_thatShouldNotBeReported() = {
    createComposerLock(composer.ComposerPackages(composer.ComposerPackage("vendor/pkg", "1.0.2", isDev = true)))

    checkInspection(
      """
        |{
        |  "require-dev": {
        |    "vendor/pkg": "1.0.2"
        |  }
        |}
      """.stripMargin)
  }

  def testGivenInstalledDevPackage_thatPackageIsOnlyInRequire_thatShouldBeReported() = {
    createComposerLock(composer.ComposerPackages(composer.ComposerPackage("vendor/pkg", "1.0.2", isDev = true)))

    checkInspection(
      """
        |{
        |  "require": {
        |    <weak_warning>"vendor/pkg": "1.0.2"</weak_warning>
        |  }
        |}
      """.stripMargin)
  }
}
