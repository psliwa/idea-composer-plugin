package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.{PsiFile, PsiElement}
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.json._
import org.psliwa.idea.composerJson.inspection.QuickFix._

import scala.annotation.tailrec

class CreatePropertyQuickFix(element: PsiElement, propertyName: String, propertySchema: Schema) extends LocalQuickFixOnPsiElement(element) {
  override def getText: String = ComposerBundle.message("inspection.quickfix.createProperty", propertyName)

  override def invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
    for {
      jsonObject <- ensureJsonObject(startElement)
      document <- documentFor(project, file)
    } yield {
      val (headOffset, trailingOffset) = createProperty(project, document, jsonObject)
      CodeStyleManager.getInstance(project).reformatText(file, headOffset, trailingOffset)
    }
  }

  private def createProperty(project: Project, document: Document, e: JsonObject): (Int, Int) = {
    val offset = e.getTextRange.getStartOffset + 1
    document.insertString(offset, "\n")

    val emptyValue = getEmptyValue(propertySchema)
    val propertyCode = "\""+propertyName+"\": "+emptyValue
    val fixedPropertyCode = propertyCode+(if(e.getChildren.length > 0) "," else "")+(if(e.getText.contains("\n")) "" else "\n")
    document.insertString(offset+1, fixedPropertyCode)

    Option(FileEditorManager.getInstance(project).getSelectedTextEditor)
      .foreach(_.getCaretModel.moveToOffset(offset + 1 + propertyCode.length - emptyValue.length/2))

    (offset, offset + fixedPropertyCode.length + 1)
  }

  @tailrec
  private def getEmptyValue(s: Schema): String = s match {
    case SObject(_, _) => "{}"
    case SArray(_) => "[]"
    case SString(_) | SStringChoice(_) => "\"\""
    case SOr(h::_) => getEmptyValue(h)
    case _ => ""
  }

  private def ensureJsonObject(e: PsiElement): Option[JsonObject] = e match {
    case x: JsonObject => Some(x)
    case _ => None
  }

  override def getFamilyName: String = ComposerBundle.message("inspection.group")
}
