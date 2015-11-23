package org.psliwa.idea.composerJson.intellij.codeAssist.composer.versionRenderer

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.{CaretAdapter, CaretEvent}

import scala.collection.mutable

class VersionOverlay(editorFactory: EditorFactory) extends ApplicationComponent {

  private val packageVersionsMap = mutable.Map[String, List[PackageVersion]]()
  private val caretListener = new CaretListener()

  override def initComponent(): Unit = {
    editorFactory.getEventMulticaster.addCaretListener(caretListener)
  }

  override def disposeComponent(): Unit = {
    editorFactory.getEventMulticaster.removeCaretListener(caretListener)
  }

  def setPackageVersions(filePath: String, packageVersions: List[PackageVersion]) = {
    packageVersionsMap(filePath) = packageVersions
    caretListener.refresh()
  }

  private[versionRenderer] def getPackageVersions(filePath: String): List[PackageVersion] = {
    packageVersionsMap.getOrElse(filePath, List())
  }

  private[versionRenderer] def clearPackageVersions() = {
    packageVersionsMap.clear()
    caretListener.refresh()
  }

  override def getComponentName: String = "composer.editorOverlay"

  private class CaretListener extends CaretAdapter {
    var listener = newVersionCaretListener

    override def caretPositionChanged(e: CaretEvent) = {
      listener.caretPositionChanged(e)
    }

    private def newVersionCaretListener = new VersionCaretListener(packageVersionsMap.toMap)

    def refresh() = {
      listener = newVersionCaretListener
    }
  }
}