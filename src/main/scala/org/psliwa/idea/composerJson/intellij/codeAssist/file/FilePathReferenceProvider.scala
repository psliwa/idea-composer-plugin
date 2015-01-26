package org.psliwa.idea.composerJson.intellij.codeAssist.file

import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet
import com.intellij.psi.{PsiElement, PsiReference, PsiReferenceProvider}
import com.intellij.util.ProcessingContext

private object FilePathReferenceProvider extends PsiReferenceProvider {
  private type PsiReferences[_] = Array[PsiReference]

  override def getReferencesByElement(element: PsiElement, context: ProcessingContext): Array[PsiReference] = {
    new FileReferenceSet(element) {
      override def isEndingSlashNotAllowed: Boolean = false
    }.getAllReferences.to[PsiReferences]
  }
}
