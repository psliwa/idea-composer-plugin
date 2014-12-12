package org.psliwa.idea.composer

import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement

package object idea {
  protected[idea] val emptyNamePlaceholder = "IntellijIdeaRulezzz"

  protected[idea] type Capture = PsiElementPattern.Capture[_ <: PsiElement]
  protected[idea] type Keywords = () => Iterable[Keyword]
}
