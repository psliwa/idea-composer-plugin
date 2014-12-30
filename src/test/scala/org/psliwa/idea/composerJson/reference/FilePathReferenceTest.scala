package org.psliwa.idea.composerJson.reference

import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.psliwa.idea.composerJson._
import org.junit.Assert._

class FilePathReferenceTest extends LightPlatformCodeInsightFixtureTestCase {

  def testGivenFileInArrayOfFilePaths_referenceShouldBeCreated() = {
    val file = "file.txt"

    checkFileReference(file,
      s"""
        |{
        |  "bin": [ "$file<caret>" ]
        |}
      """.stripMargin
    )
  }

  def testGivenFileInFilePathsObject_referenceShouldBeCreated() = {
    val file = "file.txt"

    checkFileReference(file,
      s"""
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": "$file<caret>"
        |    }
        |  }
        |}
      """.stripMargin
    )
  }

  def testGivenFileInArrayInFilePathsObject_referenceShouldBeCreated() = {
    val file = "file.txt"

    checkFileReference(file,
      s"""
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": ["$file<caret>"]
        |    }
        |  }
        |}
      """.stripMargin
    )
  }

  def testGivenNonFilePathProperty_referenceShouldNotBeCreated() = {
    val file = "file.txt"

    checkEmptyFileReferences(file,
      s"""
        |{
        |  "name": "$file<caret>"
        |}
      """.stripMargin
    )
  }

  private def checkFileReference(file: String, s: String): Unit = {
    assertEquals(1, getResolvedFileReferences(file, s).length)
  }

  private def checkEmptyFileReferences(file: String, s: String): Unit = {
    assertEquals(0, getResolvedFileReferences(file, s).length)
  }

  private def getResolvedFileReferences(file: String, s: String) = {
    myFixture.getTempDirFixture.createFile(file)

    myFixture.configureByText(ComposerJson, s)

    val element = myFixture.getFile.findElementAt(myFixture.getCaretOffset).getParent

    element.getReferences.map(_.resolve()).filter(ref => ref.isInstanceOf[PsiFile] && ref.asInstanceOf[PsiFile].getName == file)
  }
}
