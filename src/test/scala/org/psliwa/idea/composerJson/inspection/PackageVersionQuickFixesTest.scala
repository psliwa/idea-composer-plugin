package org.psliwa.idea.composerJson.inspection

import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.settings.ComposerJsonSettings
import org.junit.Assert._

class PackageVersionQuickFixesTest extends InspectionTest {
  val ExcludePatternQuickFix = ComposerBundle.message("inspection.quickfix.excludePackagePattern", _: String)

  override def setUp(): Unit = {
    super.setUp()

    ComposerJsonSettings(myFixture.getProject).getUnboundedVersionInspectionSettings.clear()
  }

  def testExcludePatternQuickFix_givenExactPattern() = {
    val pkg = "vendor/pkg321"
    checkQuickFix(ExcludePatternQuickFix(pkg))(
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
    checkQuickFix(ExcludePatternQuickFix("vendor/*"))(
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

  private def assertPatternExcluded(pkg: String) {
    assertTrue(
      ComposerJsonSettings(myFixture.getProject).getUnboundedVersionInspectionSettings.getExcludedPatterns.exists(_.getPattern == pkg)
    )
  }

  private def checkQuickFix(quickFix: String)(actual: String): Unit = {
    super.checkQuickFix(quickFix)(actual, actual)
  }
}
