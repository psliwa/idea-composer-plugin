package org.psliwa.idea.composerJson.intellij.codeAssist.php

import com.intellij.codeInsight.completion.{InsertHandler, InsertionContext}
import com.intellij.codeInsight.lookup.LookupElement
import com.jetbrains.php.lang.psi.elements.PhpClass
import PhpUtils._

private object PhpClassInsertHandler extends InsertHandler[LookupElement] {
  override def handleInsert(context: InsertionContext, item: LookupElement): Unit = {
    for {
      phpClass <- ensurePhpClass(item.getObject)
    } yield {
      val document = context.getDocument
      document.insertString(context.getTailOffset, "::")
      context.getEditor.getCaretModel.moveToOffset(context.getTailOffset)
      document.insertString(context.getStartOffset, getFixedFQNamespace(phpClass))
    }
  }

  private def ensurePhpClass(o: AnyRef): Option[PhpClass] = o match {
    case x: PhpClass => Option(x)
    case _ => None
  }
}
