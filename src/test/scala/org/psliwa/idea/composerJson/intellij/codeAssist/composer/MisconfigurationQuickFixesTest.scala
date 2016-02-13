package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest

class MisconfigurationQuickFixesTest extends InspectionTest {

  val FixPreferStable = ComposerBundle.message("inspection.quickfix.setPropertyValue", "prefer-stable", "true")
  val FixMinimumStability = ComposerBundle.message("inspection.quickfix.setPropertyValue", "minimum-stability", "stable")
  val CreateNameProperty = ComposerBundle.message("inspection.quickfix.createProperty", "name")
  val CreateLicenseProperty = ComposerBundle.message("inspection.quickfix.createProperty", "license")
  val SetTypeToComposerPlugin = ComposerBundle.message("inspection.quickfix.setPropertyValue", "type", "composer-plugin")

  val atLeastOne = new Range(Some(1), None)

  override def setUp(): Unit = {
    super.setUp()

    myFixture.enableInspections(classOf[MisconfigurationInspection])
  }

  def testSetPreferStableQuickFix_preferStableIsMissing_createPreferStableProperty() = {
    checkQuickFix(FixPreferStable, atLeastOne)(
      """
        |{
        |  "type": "project",
        |  "minimum-stability": "dev<caret>"
        |}
      """.stripMargin,
      """
        |{
        |  "type": "project",
        |  "minimum-stability": "dev",
        |  "prefer-stable": true
        |}
      """.stripMargin
    )
  }

  def testSetPreferStableQuickFix_preferStableIsFalse_setTrueValue() = {
    checkQuickFix(FixPreferStable, atLeastOne)(
      """
        |{
        |  "type": "project",
        |  "minimum-stability": "dev<caret>",
        |  "prefer-stable": false
        |}
      """.stripMargin,
      """
        |{
        |  "type": "project",
        |  "minimum-stability": "dev",
        |  "prefer-stable": true
        |}
      """.stripMargin
    )
  }

  def testSetPreferStableQuickFix_preferStableHasNoValue_setTrueValue() = {
    checkQuickFix(FixPreferStable, atLeastOne)(
      """
        |{
        |  "type": "project",
        |  "minimum-stability": "dev<caret>",
        |  "prefer-stable":
        |}
      """.stripMargin,
      """
        |{
        |  "type": "project",
        |  "minimum-stability": "dev",
        |  "prefer-stable": true
        |}
      """.stripMargin
    )
  }

  def testSetMinimumStabilityQuickFix_setStableMinimumStability() = {
    checkQuickFix(FixMinimumStability, atLeastOne)(
      """
        |{
        |  "type": "project<caret>",
        |  "minimum-stability": "dev"
        |}
      """.stripMargin,
      """
        |{
        |  "type": "project",
        |  "minimum-stability": "stable"
        |}
      """.stripMargin
    )
  }

  def testSetMinimumStabilityQuickFix_setStableMinimumStabilityForRootProperty() = {
    checkQuickFix(FixMinimumStability, atLeastOne)(
      """
        |{
        |  "type": "project<caret>",
        |  "someObject": {
        |    "minimum-stability": "dev"
        |  },
        |  "minimum-stability": "dev"
        |}
      """.stripMargin,
      """
        |{
        |  "type": "project",
        |  "someObject": {
        |    "minimum-stability": "dev"
        |  },
        |  "minimum-stability": "stable"
        |}
      """.stripMargin
    )
  }

  def testFixComposerInstallerType_setTypeToComposerPlugin() = {
    checkQuickFix(SetTypeToComposerPlugin)(
      """
        |{
        |  "type": "composer-installer<caret>"
        |}
      """.stripMargin,
      """
        |{
        |  "type": "composer-plugin"
        |}
      """.stripMargin
    )
  }

  def testFixMissingLicense_createLicenseProperty() = {
    checkQuickFix(CreateLicenseProperty)(
      """
        |{
        |  "name": "a/a<caret>"
        |}
      """.stripMargin,
      """
        |{
        |  "name": "a/a",
        |  "license": "<caret>"
        |}
      """.stripMargin
    )
  }
}
