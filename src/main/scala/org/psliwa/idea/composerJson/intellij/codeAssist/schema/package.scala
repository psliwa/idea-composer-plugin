package org.psliwa.idea.composerJson.intellij

import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement

package object schema {

  private[schema] type Capture = PsiElementPattern.Capture[_ <: PsiElement]
}
