package org.psliwa.idea.composerJson.inspection

import com.intellij.psi.{PsiFile, PsiElement}

import scala.annotation.tailrec

private[inspection] object QuickFix {
  def getHeadOffset(e: PsiElement): Int = {
    @tailrec
    def loop(e: PsiElement, offset: Int): Int = {
      e match {
        case _: PsiFile => offset
        case _ => loop(e.getParent, e.getStartOffsetInParent + offset)
      }
    }

    loop(e, 0)
  }
}
