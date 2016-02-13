package org.psliwa.idea.composerJson.intellij.codeAssist.file

import com.intellij.psi.{PsiElement, PsiReference, PsiReferenceProvider}
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson.json.{EmailFormat, UriFormat}

private object UrlReferenceProvider extends PsiReferenceProvider {
  override def getReferencesByElement(element: PsiElement, context: ProcessingContext): Array[PsiReference] = {
    val text = element.getText.substring(1, element.getText.length-1)

    if(EmailFormat.isValid(text)) {
      Array(new UrlPsiReference(element) {
        override protected def url: String = "mailto:" + super.url
      })
    } else if(UriFormat.isValid(text)) {
      Array(new UrlPsiReference(element))
    } else {
      Array()
    }
  }
}
