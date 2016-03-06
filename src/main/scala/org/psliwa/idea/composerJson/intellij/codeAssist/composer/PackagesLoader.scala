package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.fileEditor.{FileEditorManager, FileEditorManagerAdapter, FileEditorManagerListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.composer.repository._
import org.psliwa.idea.composerJson.intellij.codeAssist.BaseLookupElement
import org.psliwa.idea.composerJson.settings.ComposerJsonSettings
import org.psliwa.idea.composerJson.util.Funcs._

import scala.collection.{mutable, Seq}

class PackagesLoader extends ApplicationComponent {
  private val repositoryProviders = mutable.Map[Project,RepositoryProvider[_ <: BaseLookupElement]]()

  private lazy val loadPackageLookupElements = loadPackages.map(new BaseLookupElement(_, Some(Icons.Packagist)))
  private lazy val loadPackages = {
    if(isUnitTestMode) Nil
    else Packagist.loadPackages(Packagist.defaultUrl).getOrElse(Nil)
  }
  private val versionsLoader: (String) => Seq[String] = memorize(30)(Packagist.loadVersions(Packagist.defaultUrl)(_).getOrElse(List()))
  private lazy val packagistRepository = new CallbackRepository(loadPackageLookupElements, versionsLoader)

  private val defaultRepositoryProvider = new DefaultRepositoryProvider(packagistRepository, new BaseLookupElement(_))

  override def initComponent(): Unit = {
    val app = ApplicationManager.getApplication
    val bus = app.getMessageBus.connect(app)

    //load packages first time, when composer.json file is opened
    bus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerAdapter{
      override def fileOpened(source: FileEditorManager, file: VirtualFile): Unit = file.getName match {
        case ComposerJson => app.executeOnPooledThread(new Runnable {
          override def run(): Unit = loadPackageLookupElements
        })
        case _ =>
      }
    })
  }

  override def disposeComponent(): Unit = {
    repositoryProviders.clear()
  }
  override def getComponentName: String = "composer.packagesLoader"

  def repositoryProviderFor(project: Project): RepositoryProvider[_ <: BaseLookupElement] = {
    repositoryProviders.getOrElseUpdate(project, createRepositoryProvider(project))
  }

  private def createRepositoryProvider(project: Project): RepositoryProvider[_ <: BaseLookupElement] = {
    val settings = ComposerJsonSettings.getInstance(project)
    if(isUnitTestMode) new TestingRepositoryProvider
    else {
      new RepositoryProviderWrapper(
        defaultRepositoryProvider,
        packagistRepository,
        file => !settings.getCustomRepositoriesSettings.isEnabled(file)
      )
    }
  }

  private def isUnitTestMode = ApplicationManager.getApplication.isUnitTestMode
}
