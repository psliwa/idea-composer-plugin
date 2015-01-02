package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.json.psi.{JsonElement, JsonObject}
import com.intellij.lang.ASTNode
import com.intellij.openapi.editor.{Editor, Document}
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.{PsiFile, PsiElement}
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.json._
import org.psliwa.idea.composerJson.util.Matcher
import org.psliwa.idea.composerJson.util.PsiElementOffsetFinder._
import org.psliwa.idea.composerJson.util.PsiElementOffsetFinder.ImplicitConversions._
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
    val (previousElement, offset, nextElement) = findOffsetToInsert(e)

    val prefixText = previousElement
      .filter(_ => nextElement.isEmpty)
      .map(_ => ",")
      .getOrElse("")+"\n"

    document.insertString(offset, prefixText)

    val emptyValue = getEmptyValue(propertySchema)
    val propertyCode = "\""+propertyName+"\": "+emptyValue
    val fixedPropertyCode = propertyCode+(if(nextElement.isDefined) "," else "")+"\n"
    document.insertString(offset+prefixText.length, fixedPropertyCode)

    getEditor(project)
      .foreach(_.getCaretModel.moveToOffset(offset + prefixText.length + propertyCode.length - emptyValue.length/2))

    (offset, offset + fixedPropertyCode.length + prefixText.length)
  }

  def getEditor(project: Project): Option[Editor] = {
    Option(FileEditorManager.getInstance(project).getSelectedTextEditor)
  }

  private def findOffsetToInsert(implicit e: JsonObject): (Option[PsiElement], Int, Option[PsiElement]) = {

    val startOffset = e.getTextRange.getStartOffset + 1
    val headOffset = getEditor(e.getProject).map(_.getCaretModel.getOffset).getOrElse(startOffset)

    val jsonElement: Matcher[PsiElement] = classOf[JsonElement]

    val insertInfo = for {
      offset <- findOffsetReverse(jsonElement)(headOffset)
      element <- Option(objectAt(e, offset))
    } yield {
      (Some(element), getEndOffset(findTrailingComma(element).getOrElse(element)))
    }

    insertInfo.orElse(Some((None, startOffset)))
      .map(info => (info._1, info._2, e.getChildren.find(getEndOffset(_) > info._2)))
      .get
  }

  private def getEndOffset(element: PsiElement) = element.getTextRange.getEndOffset
  private def findTrailingComma(element: PsiElement): Option[LeafPsiElement] = findNextComma(element.getNode.getTreeNext)
  private def findNextComma(node: ASTNode): Option[LeafPsiElement] = {
    node match {
      case PsiExtractors.PsiWhiteSpace() => findNextComma(node.getTreeNext)
      case x@PsiExtractors.LeafPsiElement(",") => Some(x)
      case _ => None
    }
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
