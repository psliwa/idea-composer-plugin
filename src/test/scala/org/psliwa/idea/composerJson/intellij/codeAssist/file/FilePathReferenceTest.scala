package org.psliwa.idea.composerJson.intellij.codeAssist.file

import com.intellij.psi.{PsiElement, PsiFileSystemItem}
import org.junit.Assert._
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.intellij.codeAssist.CompletionTest

class FilePathReferenceTest extends CompletionTest {

  def testGivenFileInArrayOfFilePaths_referenceShouldBeCreated(): Unit = {
    val file = "file.txt"

    checkFileReference(file, s"""
        |{
        |  "bin": [ "$file<caret>" ]
        |}
      """.stripMargin)
  }

  def testGivenFileInFilePathsObject_referenceShouldBeCreated(): Unit = {
    val file = "file.txt"

    checkFileReference(
      file,
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

  def testGivenFileInArrayInFilePathsObject_referenceShouldBeCreated(): Unit = {
    val file = "file.txt"

    checkFileReference(
      file,
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

  def testGivenNonFilePathProperty_referenceShouldNotBeCreated(): Unit = {
    val file = "file.txt"

    checkEmptyFileReferences(file, s"""
        |{
        |  "name": "$file<caret>"
        |}
      """.stripMargin)
  }

  def testGivenRequireProperty_referenceToVendorDirShouldBeCreated(): Unit = {
    writeAction(() => {
      myFixture.getTempDirFixture
        .findOrCreateDir("vendor")
        .createChildDirectory(this, "some-vendor")
        .createChildDirectory(this, "some-pkg")
    })

    val references = getResolvedFileReferences(
      _.contains("vendor"),
      """
        |{
        |  "require": {
        |    "some-vendor/some-pkg<caret>": ""
        |  }
        |}
      """.stripMargin
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

  private def getResolvedFileReferences(fileComparator: String => Boolean,
                                        s: String,
                                        mapElement: PsiElement => PsiElement = _.getParent) = {
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
