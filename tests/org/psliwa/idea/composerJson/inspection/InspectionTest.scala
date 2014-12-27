package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.psliwa.idea.composerJson._

abstract class InspectionTest extends LightPlatformCodeInsightFixtureTestCase {
  override def isWriteActionRequired: Boolean = false

  def checkInspection(s: String): Unit = {
    myFixture.configureByText(ComposerJson, s.replace("\r", ""))
    myFixture.checkHighlighting()
  }

  def checkQuickFix[QuickFix >: LocalQuickFix](actual: String, expected: String) = {
    import scala.collection.JavaConversions._

    myFixture.configureByText(ComposerJson, actual.replace("\r", ""))
    myFixture.getAllQuickFixes(ComposerJson).foreach(myFixture.launchAction)
    myFixture.checkResult(expected.replace("\r", ""))
  }
}
