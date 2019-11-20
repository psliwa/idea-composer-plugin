package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Assert._
import org.psliwa.idea.composerJson._

abstract class InspectionTest extends BasePlatformTestCase {
  override def isWriteActionRequired: Boolean = false

  def checkInspection(s: String, filePath: String = ComposerJson): Unit = {
    filePath.split("/").toList match {
      case dir :: file :: Nil =>
        val composerJson = myFixture.configureByText(file, s.replace("\r", ""))
        writeAction(() => composerJson.getVirtualFile.move(this, findOrCreateDir(dir)))
        myFixture.testHighlighting(filePath)
      case file :: Nil =>
        myFixture.configureByText(file, s.replace("\r", ""))
        myFixture.checkHighlighting()
      case _ => fail(s"only file name or file name with one parent dir are supported as filePath, $filePath given")
    }
  }

  protected def findOrCreateDir(dir: String) = myFixture.getTempDirFixture.findOrCreateDir(dir)

  def checkQuickFix(quickFix: String, expectedQuickFixCount: Int = 1)(actual: String, expected: String): Unit = {
    checkQuickFix(quickFix, Range(Some(expectedQuickFixCount), Some(expectedQuickFixCount)))(actual, expected)
  }

  def checkQuickFix(quickFix: String, expectedQuickFixCount: Range)(actual: String, expected: String): Unit = {
    runQuickFix(quickFix, expectedQuickFixCount)(actual)

    myFixture.checkResult(expected.replace("\r", ""))
  }

  def runQuickFix(quickFix: String, expectedQuickFixCount: Int = 1)(actual: String): Unit = {
    runQuickFix(quickFix, Range(Some(expectedQuickFixCount), Some(expectedQuickFixCount)))(actual)
  }

  def runQuickFix(quickFix: String, expectedQuickFixCount: Range)(actual: String): Unit = {
    import scala.jdk.CollectionConverters._

    myFixture.configureByText(ComposerJson, actual.replace("\r", ""))

    val caretOffset = myFixture.getEditor.getCaretModel.getOffset

    //side effect of getAllQuickFixes - caret is moved to "0" offset
    val quickFixes = myFixture.getAllQuickFixes(ComposerJson).asScala.filter(qf => qf.getFamilyName.contains(quickFix) || qf.getText.contains(quickFix))
    val quickFixesCount = quickFixes.length

    val msg = s"Expected $expectedQuickFixCount '$quickFix' quick fix, $quickFixesCount found"
    expectedQuickFixCount.from.foreach(expected => assertTrue(msg, expected <= quickFixesCount))
    expectedQuickFixCount.to.foreach(expected => assertTrue(msg, expected >= quickFixesCount))

    myFixture.getEditor.getCaretModel.moveToOffset(caretOffset)
    quickFixes.take(1).foreach(myFixture.launchAction)
  }

  def writeAction(f: () => Unit): Unit = {
    ApplicationManager.getApplication.runWriteAction(new Computable[Unit] {
      override def compute = f()
    })
  }

  case class Range(from: Option[Int], to: Option[Int])
}
