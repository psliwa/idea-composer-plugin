package org.psliwa.idea.composerJson.inspection

import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.psi.PsiElement

class UninstalledPackageAnnotator extends Annotator {
  override def annotate(element: PsiElement, holder: AnnotationHolder): Unit = {
//    element.getContainingFile.getVirtualFile.
  }
}
