package org.psliwa.idea.composerJson.reference

import com.intellij.psi.{PsiFileSystemItem, PsiElement, PsiFile}
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

  def testGivenRequireProperty_referenceToVendorDirShouldBeCreated() = {

    myFixture.getTempDirFixture
      .findOrCreateDir("vendor")
      .createChildDirectory(this, "some-vendor")
      .createChildDirectory(this, "some-pkg")

    val references =  getResolvedFileReferences(_.contains("vendor"),
      """
        |{
        |  "require": {
        |    "some-vendor/some-pkg<caret>": ""
        |  }
        |}
      """.stripMargin,
      e => e.getParent.getParent
    )

    assertEquals(2, references.length)
  }

  private def checkFileReference(file: String, s: String): Unit = {
    myFixture.getTempDirFixture.createFile(file)
    assertEquals(1, getResolvedFileReferences(endsWith(file), s).length)
  }

  private def checkEmptyFileReferences(file: String, s: String): Unit = {
    myFixture.getTempDirFixture.createFile(file)

    assertEquals(0, getResolvedFileReferences(endsWith(file), s).length)
  }

  private def endsWith(suffix: String)(s: String) = s.endsWith(suffix)

  private def getResolvedFileReferences(fileComparator: String => Boolean, s: String, mapElement: PsiElement => PsiElement = _.getParent) = {
    myFixture.configureByText(ComposerJson, s)

    val element = mapElement(myFixture.getFile.findElementAt(myFixture.getCaretOffset))

    element.getReferences
      .map(_.resolve())
      .filter(_.isInstanceOf[PsiFileSystemItem])
      .map(_.asInstanceOf[PsiFileSystemItem])
      .map(_.getVirtualFile.getCanonicalPath)
      .filter(fileComparator)
  }
}
