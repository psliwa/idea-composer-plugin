package org.psliwa.idea.composerJson.intellij.codeAssist.composer.infoRenderer

import com.intellij.json.highlighting.JsonSyntaxHighlighterFactory
import com.intellij.openapi.editor.{Editor, LogicalPosition}
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.event.{CaretAdapter, CaretEvent}
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.util.text.StringUtil
import org.psliwa.idea.composerJson.ComposerJson

private class PackageInfoCaretListener(packagesInfoMap: Map[String, List[PackageInfo]]) extends CaretAdapter {

  override def caretPositionChanged(caretEvent: CaretEvent): Unit = {
    caretEvent.getEditor match {
      case editor: EditorEx if Option(editor.getVirtualFile).exists(_.getName == ComposerJson) =>
        caretPositionChanged(editor, caretEvent.getNewPosition)

      case _ =>
    }
  }

  private def caretPositionChanged(editor: EditorEx, position: LogicalPosition): Unit = {
    val color = editor.getColorsScheme.getAttributes(JsonSyntaxHighlighterFactory.JSON_BLOCK_COMMENT).getForegroundColor
    val font = editor.getColorsScheme.getFont(EditorFontType.CONSOLE_ITALIC)

    removeOverlays(editor)

    textToRender(editor, position) match {
      case Some((text, textPosition)) =>
        val component =
          new PackageInfoOverlayView(editor, editor.logicalPositionToOffset(textPosition), text, color, font)
        editor.getContentComponent.add(component)
        val innerViewpoint = editor.getScrollPane.getViewport.getView
        component.setBounds(0, 0, innerViewpoint.getWidth, innerViewpoint.getHeight)

      case None =>
    }
  }

  private def removeOverlays(editor: EditorEx): Unit = {
    editor.getContentComponent.getComponents.collect {
      case overlay: PackageInfoOverlayView => overlay
    } foreach editor.getContentComponent.remove
  }

  private def textToRender(editor: EditorEx, position: LogicalPosition): Option[(String, LogicalPosition)] = {
    (for {
      packageVersion <- packagesInfoMap.getOrElse(editor.getVirtualFile.getCanonicalPath, List.empty).view
      offset <- endLineOffset(editor, packageVersion.offset)
      packageVersionPosition = editor.offsetToLogicalPosition(offset)
      if position.line == packageVersionPosition.line
    } yield (packageVersion.info, packageVersionPosition)).headOption
  }

  private def endLineOffset(editor: Editor, offset: Int): Option[Int] = {
    lineNumber(editor, offset).map(editor.getDocument.getLineEndOffset)
  }

  private def lineNumber(editor: Editor, offset: Int): Option[Int] = {
    val lineNumber = StringUtil.offsetToLineNumber(editor.getDocument.getCharsSequence, offset)

    if (lineNumber >= 0) Option(lineNumber)
    else None
  }
}
