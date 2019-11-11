package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.openapi.vfs.VirtualFile
import org.junit.Assert._
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.composer.model.PackageDescriptor
import org.psliwa.idea.composerJson.fixtures.ComposerFixtures
import org.psliwa.idea.composerJson.fixtures.ComposerFixtures.ComposerPackageWithReplaces
import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest
import org.psliwa.idea.composerJson.settings.ProjectSettings

class PackageVersionQuickFixesTest extends InspectionTest {
  val ExcludePatternQuickFix = ComposerBundle.message("inspection.quickfix.excludePackagePattern", _: String)
  val SetPackageVersionQuickFix = ComposerBundle.message("inspection.quickfix.setPackageVersion", _: String)
  val SetEquivalentPackageVersionQuickFix = ComposerBundle.message("inspection.quickfix.setPackageEquivalentVersion", _: String)

  override def setUp(): Unit = {
    super.setUp()

    ProjectSettings(myFixture.getProject).getUnboundedVersionInspectionSettings.clear()
  }

  def testExcludePatternQuickFix_givenExactPattern() = {
    val pkg = "vendor/pkg321"
    checkQuickFix(ExcludePatternQuickFix(pkg),
      s"""
        |{
        |  "require": {
        |    "$pkg": ">2<caret>"
        |  }
        |}
      """.stripMargin
    )

    assertPatternExcluded(pkg)
  }

  def testExcludePatternQuickFix_givenVendorWildcardPattern() = {
    val pkg = "vendor/pkg321"
    checkQuickFix(ExcludePatternQuickFix("vendor/*"),
      s"""
        |{
        |  "require": {
        |    "$pkg": ">2<caret>"
        |  }
        |}
      """.stripMargin
    )

    assertPatternExcluded("vendor/*")
  }

  def testSetVersionQuickFix_givenGtOperator_replaceByNsrOperator() = {
    checkQuickFix(SetPackageVersionQuickFix("~1.3"))(
      """
        |{
        |  "require": {
        |    "vendor/pkg": ">1.2"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "require": {
        |    "vendor/pkg": "~1.3"
        |  }
        |}
      """.stripMargin
    )
  }

  def testSetVersionQuickFix_givenGteOperator_replaceByNsrOperator() = {
    checkQuickFix(SetPackageVersionQuickFix("~1.2"))(
      """
        |{
        |  "require": {
        |    "vendor/pkg": ">=1.2"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "require": {
        |    "vendor/pkg": "~1.2"
        |  }
        |}
      """.stripMargin
    )
  }

  def testSetVersionQuickFix_givenGteOperator_givenInnerConstraintIsComposed_replaceByNsrOperator() = {
    checkQuickFix(SetPackageVersionQuickFix("~1.2-beta"))(
      """
        |{
        |  "require": {
        |    "vendor/pkg": ">=1.2-beta"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "require": {
        |    "vendor/pkg": "~1.2-beta"
        |  }
        |}
      """.stripMargin
    )
  }

  def testSetVersionQuickFix_givenLogicalConstraint_logicalSeparatorShouldBeTheSameAsOriginal() = {
    checkQuickFix(SetPackageVersionQuickFix("~1.2|1.2.3"))(
      """
        |{
        |  "require": {
        |    "vendor/pkg": ">=1.2|1.2.3"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "require": {
        |    "vendor/pkg": "~1.2|1.2.3"
        |  }
        |}
      """.stripMargin
    )
  }

  def testSetVersionQuickFix_givenLogicalConstraint_givenDefaultSeparator_logicalSeparatorShouldBeTheSameAsOriginal() = {
    checkQuickFix(SetPackageVersionQuickFix("~1.2 || 1.2.3"))(
      """
        |{
        |  "require": {
        |    "vendor/pkg": ">=1.2 || 1.2.3"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "require": {
        |    "vendor/pkg": "~1.2 || 1.2.3"
        |  }
        |}
      """.stripMargin
    )
  }

  def testSetVersionQuickFix_givenWildcardAndComparisonCombination() = {
    checkQuickFix(SetPackageVersionQuickFix("<=1.2"))(
      """
        |{
        |  "require": {
        |    "vendor/pkg": "<=1.2.*"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "require": {
        |    "vendor/pkg": "<=1.2"
        |  }
        |}
      """.stripMargin
    )
  }

  def testSetVersionQuickFix_givenWrappedWildcardAndComparisonCombination() = {
    checkQuickFix(SetPackageVersionQuickFix("<=1.2@dev"))(
      """
        |{
        |  "require": {
        |    "vendor/pkg": "<=1.2.*@dev"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "require": {
        |    "vendor/pkg": "<=1.2@dev"
        |  }
        |}
      """.stripMargin
    )
  }

  def testNormalizeNsrVersionToRange() = {
    checkQuickFix(SetEquivalentPackageVersionQuickFix(">=1.2.3 <1.3.0"))(
      """
        |{
        |  "require": {
        |    "vendor/pkg": "~1.2.3"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "require": {
        |    "vendor/pkg": ">=1.2.3 <1.3.0"
        |  }
        |}
      """.stripMargin
    )
  }

  def testGivenRequireDev_normalizeNsrVersionToRange() = {
    checkQuickFix(SetEquivalentPackageVersionQuickFix(">=1.2.3 <1.3.0"))(
      """
        |{
        |  "require-dev": {
        |    "vendor/pkg": "~1.2.3"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "require-dev": {
        |    "vendor/pkg": ">=1.2.3 <1.3.0"
        |  }
        |}
      """.stripMargin
    )
  }

  def testNormalizeRangeVersionToNsr() = {
    checkQuickFix(SetEquivalentPackageVersionQuickFix("~1.2.3"))(
      """
        |{
        |  "require": {
        |    "vendor/pkg": ">=1.2.3,<1.3.0"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "require": {
        |    "vendor/pkg": "~1.2.3"
        |  }
        |}
      """.stripMargin
    )
  }

  def testSetVersionQuickFix_givenUnboundVersion_replaceByInstalledVersion(): Unit = {
    createComposerLock(List(PackageDescriptor("vendor/pkg", "1.3.1")))

    checkQuickFix(SetPackageVersionQuickFix("1.3.1"))(
      """
        |{
        |  "require": {
        |    "vendor/pkg": "*"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "require": {
        |    "vendor/pkg": "1.3.1"
        |  }
        |}
      """.stripMargin
    )
  }

  def testSetVersionQuickFix_givenUnboundVersion_replaceByWildcardedInstalledVersion(): Unit = {
    createComposerLock(List(PackageDescriptor("vendor/pkg", "1.3.*")))

    checkQuickFix(SetPackageVersionQuickFix("1.3.*"))(
      """
        |{
        |  "require": {
        |    "vendor/pkg": "*"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "require": {
        |    "vendor/pkg": "1.3.*"
        |  }
        |}
      """.stripMargin
    )
  }

  private def assertPatternExcluded(pkg: String) {
    import scala.collection.JavaConverters._
    assertTrue(
      ProjectSettings(myFixture.getProject).getUnboundedVersionInspectionSettings.getValues().asScala.exists(_.getPattern == pkg)
    )
  }

  private def checkQuickFix(quickFix: String, actual: String): Unit = {
    checkQuickFix(quickFix)(actual, actual)
  }

  private def createComposerLock(packages: List[PackageDescriptor], dir: String = "."): VirtualFile = {
    ComposerFixtures.createComposerLock(myFixture, packages.map(ComposerPackageWithReplaces(_, Set.empty)), dir)
  }
}
