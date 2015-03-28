package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import org.junit.Assert._
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest
import org.psliwa.idea.composerJson.settings.ComposerJsonSettings

class PackageVersionQuickFixesTest extends InspectionTest {
  val ExcludePatternQuickFix = ComposerBundle.message("inspection.quickfix.excludePackagePattern", _: String)
  val SetPackageVersionQuickFix = ComposerBundle.message("inspection.quickfix.setPackageVersion", _: String)
  val SetEquivalentPackageVersionQuickFix = ComposerBundle.message("inspection.quickfix.setPackageEquivalentVersion", _: String)

  override def setUp(): Unit = {
    super.setUp()

    ComposerJsonSettings(myFixture.getProject).getUnboundedVersionInspectionSettings.clear()
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

  private def assertPatternExcluded(pkg: String) {
    assertTrue(
      ComposerJsonSettings(myFixture.getProject).getUnboundedVersionInspectionSettings.getExcludedPatterns.exists(_.getPattern == pkg)
    )
  }

  private def checkQuickFix(quickFix: String, actual: String): Unit = {
    checkQuickFix(quickFix)(actual, actual)
  }
}
