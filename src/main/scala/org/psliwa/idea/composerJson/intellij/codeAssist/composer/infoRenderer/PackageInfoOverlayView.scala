package org.psliwa.idea.composerJson.intellij.codeAssist.composer.infoRenderer

import java.awt.{Color, Font, Graphics}
import javax.swing.JComponent

import com.intellij.openapi.editor.Editor

private class PackageInfoOverlayView(editor: Editor, offset: Int, text: String, color: Color, font: Font) extends JComponent {
  private val horizontalMargin = 40

  override def paintComponent(g: Graphics): Unit = {
    g.setColor(color)
    g.setFont(font)

    val verticalAlignment = editor.getLineHeight - editor.getColorsScheme.getEditorFontSize
    val point = editor.visualPositionToXY(editor.offsetToVisualPosition(offset))
    g.drawString(text, point.x + horizontalMargin, point.y + editor.getLineHeight - verticalAlignment)
  }
}