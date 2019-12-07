package org.psliwa.idea.composerJson.intellij.codeAssist.file

import org.junit.Assert._
import org.psliwa.idea.composerJson.intellij.codeAssist.FilePathReferences

class FilePathReferenceTest extends FilePathReferences {

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
}
