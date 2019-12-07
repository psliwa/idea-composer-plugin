package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.psi.{PsiElement, PsiFileSystemItem}
import org.junit.Assert.assertEquals
import org.psliwa.idea.composerJson.ComposerJson

abstract class FilePathReferences extends CompletionTest {

  def checkFileReference(file: String, s: String): Unit = {
    myFixture.getTempDirFixture.createFile(file)
    assertEquals(1, getResolvedFileReferences(endsWith(file), s).length)
  }

  def checkEmptyFileReferences(file: String, s: String): Unit = {
    myFixture.getTempDirFixture.createFile(file)

    assertEquals(0, getResolvedFileReferences(endsWith(file), s).length)
  }

  private def endsWith(suffix: String)(s: String): Boolean = s.endsWith(suffix)

  def getResolvedFileReferences(fileComparator: String => Boolean,
                                s: String,
                                mapElement: PsiElement => PsiElement = _.getParent): Array[String] = {
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
