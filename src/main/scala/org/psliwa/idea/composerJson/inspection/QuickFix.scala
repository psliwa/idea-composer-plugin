package org.psliwa.idea.composerJson.inspection

import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiDocumentManager, PsiFile, PsiElement}

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

  def documentFor(project: Project, file: PsiFile): Option[Document] = {
    Option(PsiDocumentManager.getInstance(project).getDocument(file))
  }
}
