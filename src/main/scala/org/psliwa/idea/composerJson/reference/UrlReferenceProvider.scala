package org.psliwa.idea.composerJson.reference

import com.intellij.psi.{PsiReference, PsiElement, PsiReferenceProvider}
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson.json.{EmailFormat, UriFormat}

private object UrlReferenceProvider extends PsiReferenceProvider {
  override def getReferencesByElement(element: PsiElement, context: ProcessingContext): Array[PsiReference] = {
    val text = element.getText.substring(1, element.getText.size-1)

    if(EmailFormat.isValid(text)) {
      Array(new UrlPsiReference(element, "mailto:"))
    } else if(UriFormat.isValid(text)) {
      Array(new UrlPsiReference(element))
    } else {
      Array()
    }
  }
}
