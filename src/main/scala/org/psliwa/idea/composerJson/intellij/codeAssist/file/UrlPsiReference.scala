package org.psliwa.idea.composerJson.intellij.codeAssist.file

import com.intellij.ide.BrowserUtil
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.{NavigatablePsiElement, PsiElement, PsiReferenceBase}
import org.psliwa.idea.composerJson.intellij.PsiElementWrapper

private class UrlPsiReference(element: PsiElement) extends PsiReferenceBase[PsiElement](element) {

  protected def url: String = getValue

  def resolve: PsiElement = {
    new PsiElementWrapper(element) with NavigatablePsiElement {
      override def getParent: PsiElement = element.getParent
      override def navigate(requestFocus: Boolean) = BrowserUtil.browse(url)
      override def canNavigate: Boolean = true
      override def canNavigateToSource: Boolean = true
      override def getNavigationElement = this
      override def getName: String = url
      override def getPresentation: ItemPresentation = null
    }
  }
  def getVariants: Array[AnyRef] = UrlPsiReference.EmptyArray
  override def isSoft: Boolean = true
}

private object UrlPsiReference {
  val EmptyArray = Array[AnyRef]()
}