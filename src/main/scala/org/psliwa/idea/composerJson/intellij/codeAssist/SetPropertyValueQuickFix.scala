package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.json.psi.{JsonObject, JsonProperty}
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.{PsiElement, PsiFile}
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.json.Schema
import org.psliwa.idea.composerJson.intellij.PsiElements.findProperty
import QuickFix._

private class SetPropertyValueQuickFix(
    element: JsonObject,
    propertyName: String,
    propertySchema: Schema,
    propertyValue: String
) extends LocalQuickFixOnPsiElement(element) {
  override def getText: String =
    ComposerBundle.message("inspection.quickfix.setPropertyValue", propertyName, propertyValue)

  override def invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
    import org.psliwa.idea.composerJson.intellij.PsiExtractors.JsonProperty

    findProperty(element, propertyName) match {
      case Some(p @ JsonProperty(_, _)) => setPropertyValue(p)
      case None => createProperty()
    }
  }

  private def createProperty(): Unit = {
    new CreatePropertyQuickFix(element, propertyName, propertySchema).applyFix()

    for {
      document <- documentFor(element.getProject, element.getContainingFile)
      editor <- editorFor(element.getProject)
    } yield {
      val offset = editor.getCaretModel.getOffset

      document.insertString(editor.getCaretModel.getOffset, fixValue(propertyValue, document, offset))
    }
  }

  private def fixValue(value: CharSequence, document: Document, offset: Int): String = {
    (if (document.getCharsSequence.charAt(offset - 1) == ':') " " else "") + value
  }

  private def setPropertyValue(property: JsonProperty): Unit = {
    for {
      document <- documentFor(element.getProject, element.getContainingFile)
      editor <- editorFor(element.getProject)
      range <- Option(property.getValue).map(_.getTextRange).orElse(Some(valueTextRangeFor(property)))
    } yield {
      val wrappedValue = fixValue(wrapValue(propertyValue), document, range.getStartOffset)
      document.replaceString(range.getStartOffset, range.getEndOffset, wrappedValue)
    }
  }

  private def valueTextRangeFor(property: JsonProperty) = {
    new TextRange(property.getTextRange.getEndOffset, property.getTextRange.getEndOffset)
  }

  private def wrapValue(s: String): CharSequence = {
    val wrapper = getEmptyValue(propertySchema)
    val (prefix, suffix) = wrapper.splitAt(wrapper.length / 2)

    prefix + s + suffix
  }

  override def getFamilyName: String = ComposerBundle.message("inspection.group")
}
