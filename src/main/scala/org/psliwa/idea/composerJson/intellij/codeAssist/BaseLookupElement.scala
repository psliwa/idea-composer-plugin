package org.psliwa.idea.composerJson.intellij.codeAssist

import javax.swing.Icon

import com.intellij.codeInsight.completion.{InsertionContext, InsertHandler}
import com.intellij.codeInsight.lookup.{LookupElementPresentation, LookupElement}
import com.intellij.psi.PsiElement

private[codeAssist] case class BaseLookupElement(
   name: String,
   icon: Option[Icon] = None,
   quoted: Boolean = true,
   insertHandler: Option[InsertHandler[LookupElement]] = None,
   psiElement: Option[PsiElement] = None,
   description: String = ""
 ) extends LookupElement {

    private val presentation = new LookupElementPresentation
    presentation.setIcon(icon.orNull)
    presentation.setItemText(name)
    presentation.setTypeGrayed(true)
    presentation.setTypeText(if(description == "") null else description)
    presentation.setStrikeout(description.startsWith("DEPRECATED"))

    override def getLookupString = name
    override def renderElement(presentation: LookupElementPresentation): Unit = presentation.copyFrom(this.presentation)
    override def handleInsert(context: InsertionContext): Unit = insertHandler.foreach(_.handleInsert(context, this))

    def withInsertHandler(insertHandler: InsertHandler[LookupElement]) = {
      new BaseLookupElement(name, icon, quoted, Some(insertHandler), psiElement, description)
    }

    def withPsiElement(psiElement: PsiElement) = {
      new BaseLookupElement(name, icon, quoted, insertHandler, Some(psiElement), description)
    }

    override def getObject: AnyRef = psiElement.getOrElse(this)
  }
