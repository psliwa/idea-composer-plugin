package org.psliwa.idea.composerJson.inspection

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.psliwa.idea.composerJson._
import org.junit.Assert._

abstract class InspectionTest extends LightPlatformCodeInsightFixtureTestCase {
  override def isWriteActionRequired: Boolean = false

  def checkInspection(s: String): Unit = {
    myFixture.configureByText(ComposerJson, s.replace("\r", ""))
    myFixture.checkHighlighting()
  }

  def checkQuickFix(quickFix: String, expectedQuickFixCount: Int = 1)(actual: String, expected: String) = {
    runQuickFix(quickFix, expectedQuickFixCount)(actual)

    myFixture.checkResult(expected.replace("\r", ""))
  }

  def runQuickFix(quickFix: String, expectedQuickFixCount: Int = 1)(actual: String) = {
    import scala.collection.JavaConversions._

    myFixture.configureByText(ComposerJson, actual.replace("\r", ""))

    val caretOffset = myFixture.getEditor.getCaretModel.getOffset

    //side effect of getAllQuickFixes - caret is moved to "0" offset
    val quickFixes = myFixture.getAllQuickFixes(ComposerJson).filter(_.getFamilyName == quickFix)
    val quickFixesCount = quickFixes.length

    assertTrue(
      s"Expected $expectedQuickFixCount '$quickFix' quick fix, $quickFixesCount found",
      quickFixesCount == expectedQuickFixCount
    )

    myFixture.getEditor.getCaretModel.moveToOffset(caretOffset)
    quickFixes.take(1).foreach(myFixture.launchAction)
  }

  def writeAction(f: () => Unit) = {
    ApplicationManager.getApplication.runWriteAction(new Computable[Unit] {
      override def compute = f()
    })
  }
}
