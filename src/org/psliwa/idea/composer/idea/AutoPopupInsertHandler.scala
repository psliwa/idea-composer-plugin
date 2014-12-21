package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.completion.{InsertionContext, InsertHandler}
import com.intellij.codeInsight.lookup.LookupElement

class AutoPopupInsertHandler(
  insertHandler: Option[InsertHandler[LookupElement]],
  condition: InsertionContext => Boolean = _ => true
) extends InsertHandler[LookupElement] {
  override def handleInsert(context: InsertionContext, item: LookupElement): Unit = {
    insertHandler.foreach(_.handleInsert(context, item))

    if(condition(context)) {
      val editor = context.getEditor
      AutoPopupController.getInstance(editor.getProject).scheduleAutoPopup(editor)
    }
  }
}
