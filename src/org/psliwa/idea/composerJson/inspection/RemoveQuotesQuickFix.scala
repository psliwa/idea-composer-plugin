package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiDocumentManager, PsiFile, PsiElement}
import org.psliwa.idea.composerJson.ComposerBundle

import scala.annotation.tailrec

class RemoveQuotesQuickFix(element: PsiElement) extends LocalQuickFixOnPsiElement(element){

  def documentFor(project: Project, file: PsiFile): Option[Document] = {
    Option(PsiDocumentManager.getInstance(project).getDocument(file))
  }

  override def invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
    for {
      stringLiteral <- ensureJsonStringLiteral(element)
      document <- documentFor(project, file)
    } yield {
      val headOffset = getHeadOffset(stringLiteral)
      val trailingOffset = headOffset + stringLiteral.getText.length - 2

      document.replaceString(headOffset, headOffset+1, "")
      document.replaceString(trailingOffset, trailingOffset+1, "")
    }
  }

  private def getHeadOffset(e: PsiElement): Int = {
    @tailrec
    def loop(e: PsiElement, offset: Int): Int = {
      e match {
        case _: PsiFile => offset
        case _ => loop(e.getParent, e.getStartOffsetInParent + offset)
      }
    }

    loop(e, 0)
  }

  private def ensureJsonStringLiteral(e: PsiElement): Option[JsonStringLiteral] = e match {
    case x: JsonStringLiteral => Some(x)
    case _ => None
  }

  override def getText: String = ComposerBundle.message("inspection.quickfix.removeQuotes")
  override def getFamilyName: String = ComposerBundle.message("inspection.group")
}
