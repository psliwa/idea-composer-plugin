package org.psliwa.idea.composerJson.intellij.codeAssist

import javax.swing.Icon

import com.intellij.codeInsight.completion.{InsertHandler, InsertionContext}
import com.intellij.codeInsight.lookup.{LookupElement, LookupElementPresentation, LookupValueWithPriority}
import com.intellij.psi.PsiElement

final private[codeAssist] class BaseLookupElement(
    val name: String,
    val icon: Option[Icon] = None,
    val quoted: Boolean = true,
    val insertHandler: Option[InsertHandler[LookupElement]] = None,
    val psiElement: Option[PsiElement] = None,
    val description: String = "",
    val priority: Option[Int] = None
) extends LookupElement {

  private val presentation = new LookupElementPresentation
  presentation.setIcon(icon.orNull)
  presentation.setItemText(name)
  presentation.setTypeGrayed(true)
  presentation.setTypeText(if (description == "") null else description)
  presentation.setStrikeout(description.startsWith("DEPRECATED"))

  override def getLookupString: String = name
  override def renderElement(presentation: LookupElementPresentation): Unit = presentation.copyFrom(this.presentation)
  override def handleInsert(context: InsertionContext): Unit = insertHandler.foreach(_.handleInsert(context, this))

  def withInsertHandler(insertHandler: InsertHandler[LookupElement]): BaseLookupElement = {
    new BaseLookupElement(name, icon, quoted, Some(insertHandler), psiElement, description, priority)
  }

  def withPsiElement(psiElement: PsiElement): BaseLookupElement = {
    new BaseLookupElement(name, icon, quoted, insertHandler, Some(psiElement), description, priority)
  }

  override def getObject: AnyRef = psiElement.getOrElse(this)

  override def equals(other: Any): Boolean = other match {
    case that: BaseLookupElement =>
      name == that.name &&
        icon == that.icon &&
        quoted == that.quoted &&
        insertHandler == that.insertHandler &&
        psiElement == that.psiElement &&
        description == that.description
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(name, icon, quoted, insertHandler, psiElement, description)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}
