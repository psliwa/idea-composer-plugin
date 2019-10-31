package org.psliwa.idea.composerJson.intellij.codeAssist.composer.infoRenderer

import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.{CaretAdapter, CaretEvent}

import scala.collection.mutable

class PackageInfoOverlay(editorFactory: EditorFactory) extends ApplicationComponent {

  private val packagesInfoMap = mutable.Map[String, List[PackageInfo]]()
  private val caretListener = new CaretListener()

  override def initComponent(): Unit = {
    editorFactory.getEventMulticaster.addCaretListener(caretListener)
  }

  override def disposeComponent(): Unit = {
    editorFactory.getEventMulticaster.removeCaretListener(caretListener)
  }

  def setPackagesInfo(filePath: String, packagesInfo: List[PackageInfo]): Unit = {
    packagesInfoMap(filePath) = packagesInfo
    caretListener.refresh()
  }

  private[infoRenderer] def getPackagesInfo(filePath: String): List[PackageInfo] = {
    packagesInfoMap.getOrElse(filePath, List())
  }

  private[infoRenderer] def clearPackagesInfo(): Unit = {
    packagesInfoMap.clear()
    caretListener.refresh()
  }

  override def getComponentName: String = "composer.editorOverlay"

  private class CaretListener extends CaretAdapter {
    var listener = newCaretListener

    override def caretPositionChanged(e: CaretEvent): Unit = {
      listener.caretPositionChanged(e)
    }

    private def newCaretListener = new PackageInfoCaretListener(packagesInfoMap.toMap)

    def refresh(): Unit = {
      listener = newCaretListener
    }
  }
}