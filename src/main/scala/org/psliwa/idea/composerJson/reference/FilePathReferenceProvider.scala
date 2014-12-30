package org.psliwa.idea.composerJson.reference

import com.intellij.psi.impl.source.resolve.reference.impl.providers.{FileReference, FileReferenceSet}
import com.intellij.psi.{PsiReference, PsiElement, PsiReferenceProvider}
import com.intellij.util.ProcessingContext

private[reference] object FilePathReferenceProvider extends PsiReferenceProvider {
  private type PsiReferences[_] = Array[PsiReference]

  override def getReferencesByElement(element: PsiElement, context: ProcessingContext): Array[PsiReference] = {
    new FileReferenceSet(element) {
      override def isEndingSlashNotAllowed: Boolean = false
    }.getAllReferences.to[PsiReferences]
  }
}