package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.fileEditor.{FileEditorManager, FileEditorManagerAdapter, FileEditorManagerListener}
import com.intellij.openapi.vfs.VirtualFile
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.composer.repository.Packagist
import org.psliwa.idea.composerJson.intellij.codeAssist.BaseLookupElement

class PackagesLoader extends ApplicationComponent {
  override def initComponent(): Unit = {
    val app = ApplicationManager.getApplication
    val bus = app.getMessageBus.connect(app)

    //load packages first time, when composer.json file is opened
    bus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter{
      override def fileOpened(source: FileEditorManager, file: VirtualFile): Unit = file.getName match {
        case ComposerJson => app.executeOnPooledThread(new Runnable {
          override def run(): Unit = PackagesLoader.loadPackages
        })
        case _ =>
      }
    })
  }

  override def disposeComponent(): Unit = {}
  override def getComponentName: String = "composer.packagesLoader"
}

object PackagesLoader {
  lazy val loadPackages = {
    if(isUnitTestMode) Nil
    else Packagist.loadPackages().right.getOrElse(Nil).map(new BaseLookupElement(_, Some(Icons.Packagist)))
  }

  private def isUnitTestMode = ApplicationManager.getApplication.isUnitTestMode
}
