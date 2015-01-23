package org.psliwa.idea.composerJson.inspection

import com.intellij.openapi.editor.{Editor, Document}
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiDocumentManager, PsiFile, PsiElement}
import org.psliwa.idea.composerJson.json._

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

  def editorFor(project: Project): Option[Editor] = {
    Option(FileEditorManager.getInstance(project).getSelectedTextEditor)
  }

  @tailrec
  def getEmptyValue(s: Schema): String = s match {
    case SObject(_, _) => "{}"
    case SArray(_) => "[]"
    case SString(_) | SStringChoice(_) => "\"\""
    case SOr(h::_) => getEmptyValue(h)
    case _ => ""
  }
}
