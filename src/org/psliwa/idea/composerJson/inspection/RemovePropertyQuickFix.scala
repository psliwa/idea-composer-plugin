package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInspection.LocalQuickFixOnPsiElement

import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiFile, PsiElement}
import org.psliwa.idea.composerJson.ComposerBundle

import scala.annotation.tailrec

class RemovePropertyQuickFix(element: PsiElement) extends LocalQuickFixOnPsiElement(element) {

  import PsiExtractors.JsonProperty
  import PsiExtractors.LeafPsiElement

  override def invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
    val maybeElement = Option(startElement.getContext)

    if(maybeElement.flatMap(nextPropertyElement).isEmpty) {
      maybeElement.flatMap(previousCommaElement).foreach(_.delete())
    }

    maybeElement.flatMap(nextCommaElement).foreach(_.delete())

    maybeElement.foreach(_.delete())
  }
  
  private def nextCommaElement = findSibling(isCommaElement, x => Option(x.getNextSibling), isJsonProperty) _
  private def previousCommaElement = findSibling(isCommaElement, x => Option(x.getPrevSibling), isJsonProperty) _
  private def nextPropertyElement = findSibling(isJsonProperty, x => Option(x.getNextSibling)) _

  private def isCommaElement(e: PsiElement): Boolean = e match {
    case LeafPsiElement(",") => true
    case _ => false
  }

  private def isJsonProperty(e: PsiElement): Boolean = e match {
    case JsonProperty(_, _) => true
    case _ => false
  }

  private def findSibling(
    thatsIt: PsiElement => Boolean,
    nextSibling: PsiElement => Option[PsiElement],
    stop: PsiElement => Boolean = _ => false
  )(e: PsiElement): Option[PsiElement] = {
    @tailrec
    def loop(e: Option[PsiElement]): Option[PsiElement] = {
      e match {
        case Some(x) if thatsIt(x) => Some(x)
        case Some(x) if stop(x) => None
        case Some(x) => loop(nextSibling(x))
        case None => None
      }
    }

    loop(nextSibling(e))
  }

  override def getText: String = ComposerBundle.message("inspection.quickfix.removeProperty")
  override def getFamilyName: String = ComposerBundle.message("inspection.group")
}
