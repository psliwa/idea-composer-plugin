package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.fileEditor.{FileEditorManager, FileEditorManagerAdapter, FileEditorManagerListener}
import com.intellij.openapi.vfs.VirtualFile
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.composer.repository.{CallbackRepository, DefaultRepositoryProvider, Packagist}
import org.psliwa.idea.composerJson.intellij.codeAssist.BaseLookupElement
import org.psliwa.idea.composerJson.util.Funcs._

import scala.collection.Seq

class PackagesLoader extends ApplicationComponent {
  override def initComponent(): Unit = {
    val app = ApplicationManager.getApplication
    val bus = app.getMessageBus.connect(app)

    //load packages first time, when composer.json file is opened
    bus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter{
      override def fileOpened(source: FileEditorManager, file: VirtualFile): Unit = file.getName match {
        case ComposerJson => app.executeOnPooledThread(new Runnable {
          override def run(): Unit = PackagesLoader.loadPackageLookupElements
        })
        case _ =>
      }
    })
  }

  override def disposeComponent(): Unit = {}
  override def getComponentName: String = "composer.packagesLoader"
}

object PackagesLoader {
  private lazy val loadPackageLookupElements = loadPackages.map(new BaseLookupElement(_, Some(Icons.Packagist)))

  private lazy val loadPackages = {
    if(isUnitTestMode) Nil
    else Packagist.loadPackages().right.getOrElse(Nil)
  }
  private val versionsLoader: (String) => Seq[String] = memorize(30)(Packagist.loadVersions(_).right.getOrElse(List()))
  private lazy val packagistRepository = new CallbackRepository(loadPackageLookupElements, versionsLoader)

  lazy val repositoryProvider = new DefaultRepositoryProvider(packagistRepository, new BaseLookupElement(_))

  private def isUnitTestMode = ApplicationManager.getApplication.isUnitTestMode
}
