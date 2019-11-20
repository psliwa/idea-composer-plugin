package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.{InsertHandler, InsertionContext}
import com.intellij.codeInsight.lookup.LookupElement

private[intellij] class AutoPopupInsertHandler(
    insertHandler: Option[InsertHandler[LookupElement]],
    condition: InsertionContext => Boolean = _ => true
) extends InsertHandler[LookupElement] {

  def this(insertHandler: InsertHandler[LookupElement]) = {
    this(Some(insertHandler))
  }

  override def handleInsert(context: InsertionContext, item: LookupElement): Unit = {
    insertHandler.foreach(_.handleInsert(context, item))

    if (condition(context)) {
      val editor = context.getEditor
      AutoPopupController.getInstance(editor.getProject).scheduleAutoPopup(editor)
    }
  }
}
