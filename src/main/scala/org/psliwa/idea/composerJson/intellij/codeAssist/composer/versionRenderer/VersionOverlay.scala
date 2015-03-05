package org.psliwa.idea.composerJson.intellij.codeAssist.composer.versionRenderer

import java.awt.event._
import java.awt.{Component, Color, Font, Graphics}
import javax.swing.JComponent

import com.intellij.json.highlighting.JsonSyntaxHighlighterFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.util.text.StringUtil

import scala.collection.mutable

import org.psliwa.idea.composerJson.ComposerJson
import org.psliwa.idea.composerJson.intellij.codeAssist.QuickFix

class VersionOverlay extends ApplicationComponent {

  private val packageVersionsMap = mutable.Map[String, List[PackageVersion]]()
  private val overlays = mutable.Map[String, OverlayComponent]()

  override def initComponent(): Unit = {
    val app = ApplicationManager.getApplication
    val bus = app.getMessageBus.connect(app)

    bus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter{
      override def selectionChanged(event: FileEditorManagerEvent): Unit = {
        Option(event.getNewFile).map(_.getName).getOrElse("") match {
          case ComposerJson => {
            useEditor(event)((editor, innerViewport) => {
              val maybeOverlay = editor.getContentComponent.getComponents.find(_.isInstanceOf[OverlayComponent])
              val overlay = maybeOverlay match {
                case Some(component) => component
                case None => {
                  val color = editor.getColorsScheme.getAttributes(JsonSyntaxHighlighterFactory.JSON_BLOCK_COMMENT).getForegroundColor
                  val font = editor.getColorsScheme.getFont(EditorFontType.CONSOLE_ITALIC)
                  val filePath = event.getNewFile.getCanonicalPath

                  val component = new OverlayComponent(filePath, color, font, editor)
                  overlays(event.getNewFile.getCanonicalPath) = component

                  editor.getContentComponent.add(component)

                  innerViewport.addComponentListener(component)
                  component.resizeTo(innerViewport)

                  component
                }
              }

              overlay.invalidate()
            })
          }
          case _ =>
        }

        Option(event.getOldFile).map(_.getName).getOrElse("") match {
          case ComposerJson => {
            useEditor(event)((editor, innerViewport) => {
              editor
                .getContentComponent
                .getComponents
                .find(_.isInstanceOf[OverlayComponent])
                .map(_.asInstanceOf[OverlayComponent])
                .foreach(component => {
                  innerViewport.removeComponentListener(component)
                  editor.getContentComponent.remove(component)
                })

              overlays -= event.getOldFile.getCanonicalPath
            })
          }
          case _ =>
        }
      }
    })
  }

  private def useEditor(event: FileEditorManagerEvent)(f: (EditorImpl, Component) => Unit) = {
    for {
      e <- QuickFix.editorFor(event.getManager.getProject)
      editor = e.asInstanceOf[EditorImpl]
      innerViewport <- Option(editor.getScrollPane.getViewport.getView)
    } f(editor, innerViewport)
  }

  override def disposeComponent(): Unit = {
    packageVersionsMap.clear()
    overlays.clear()
  }

  def setPackageVersions(filePath: String, packageVersions: List[PackageVersion]) = {
    packageVersionsMap(filePath) = packageVersions
    overlays.get(filePath).foreach(_.repaint())
  }

  private[versionRenderer] def getPackageVersions(filePath: String): List[PackageVersion] = {
    packageVersionsMap.getOrElse(filePath, List())
  }

  private[versionRenderer] def clearPackageVersions() = {
    packageVersionsMap.clear()
  }

  override def getComponentName: String = "composer.editorOverlay"

  private class OverlayComponent(filePath: String, color: Color, font: Font, editor: Editor) extends JComponent with ComponentListener {
    private val horizontalMargin = 40

    override def paintComponent(g: Graphics): Unit = {
      g.setColor(color)
      g.setFont(font)

      val verticalAlignment = editor.getLineHeight - editor.getColorsScheme.getEditorFontSize
      val caretOffset = endLineOffset(editor.getCaretModel.getOffset)

      def versionPresentation(packageVersion: PackageVersion) = {
        if(packageVersion.version.length > 12) packageVersion.version.substring(0, 12) + "..."
        else packageVersion.version
      }

      for{
        packageVersion <- packageVersionsMap.getOrElse(filePath, List[PackageVersion]())
        offset = endLineOffset(packageVersion.offset) if offset == caretOffset
      } {
        val point = editor.visualPositionToXY(editor.offsetToVisualPosition(offset))
        g.drawString(versionPresentation(packageVersion), point.x + horizontalMargin, point.y + editor.getLineHeight - verticalAlignment)
      }
    }

    private def endLineOffset(offset: Int): Int = {
      editor.getDocument.getLineEndOffset(StringUtil.offsetToLineNumber(editor.getDocument.getCharsSequence, offset))
    }

    override def componentResized(e: ComponentEvent): Unit = {
      resizeTo(e.getComponent)
    }

    def resizeTo(component: Component) = {
      if(component.getWidth != getWidth || component.getHeight != getHeight) {
        setBounds(0, 0, component.getWidth, component.getHeight)
      }
    }

    override def componentMoved(e: ComponentEvent): Unit = {}
    override def componentShown(e: ComponentEvent): Unit = {}
    override def componentHidden(e: ComponentEvent): Unit = {}
  }
}
