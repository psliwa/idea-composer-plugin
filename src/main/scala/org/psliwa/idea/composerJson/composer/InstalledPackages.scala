package org.psliwa.idea.composerJson.composer

import java.io.{BufferedReader, IOException, InputStreamReader}

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ApplicationComponent
import com.intellij.openapi.vfs._
import org.psliwa.idea.composerJson.ComposerLock
import org.psliwa.idea.composerJson.composer.model.Packages
import org.psliwa.idea.composerJson.composer.parsers.JsonParsers

import scala.collection.mutable

class InstalledPackagesWatcher extends ApplicationComponent {

  //packages are lazy loaded
  private val packages = mutable.Map[VirtualFile, () => Packages]()
  private val listener = new ComposerLockListener()

  private[composer] def getPackages(file: VirtualFile): Packages = {
    val maybePackages = for {
      parent <- Option(file.getParent)
      lock <- Option(parent.findChild(ComposerLock)).filter(!_.isDirectory)
    } yield packages.getOrElse(lock, loadPackagesAndSet(lock))()

    maybePackages.getOrElse(Packages())
  }

  override def initComponent(): Unit = {
    VirtualFileManager.getInstance().addVirtualFileListener(listener)
  }

  override def disposeComponent(): Unit = {
    VirtualFileManager.getInstance().removeVirtualFileListener(listener)
    packages.clear()
  }

  override def getComponentName: String = "composer.installedPackagesWatcher"

  private[InstalledPackagesWatcher] def loadPackages(file: VirtualFile) = {
    readFile(file)
      .map(JsonParsers.parseLockPackages)
      .getOrElse(Packages())
  }

  private def loadPackagesAndSet(file: VirtualFile) = {
    lazy val loadedPackages = loadPackages(file)
    val pkgs = () => loadedPackages
    packages(file) = pkgs

    pkgs
  }

  private[InstalledPackagesWatcher] def refresh(file: VirtualFile): Unit = {
    //lazy load: parse composer.lock only once and on first read
    lazy val loadedPackages = loadPackages(file)
    packages(file) = () => loadedPackages
  }

  private[InstalledPackagesWatcher] def readFile(file: VirtualFile): Option[String] = {
    try {
      val in = new BufferedReader(new InputStreamReader(file.getInputStream))
      try {
        var line: String = null
        val content = new StringBuilder()

        while ({ line = in.readLine(); line != null }) {
          content.append(line)
        }

        Some(content.toString())
      } finally {
        in.close()
      }
    } catch {
      case _: IOException => None
    }
  }

  private class ComposerLockListener extends VirtualFileListener {

    override def fileCreated(event: VirtualFileEvent): Unit =
      ensureComposerLock(event)(() => {
        refresh(event.getFile)
      })

    private def ensureComposerLock(event: VirtualFileEvent)(f: () => Unit): Unit = {
      if (event.getFileName == ComposerLock) f()
    }

    override def contentsChanged(event: VirtualFileEvent): Unit =
      ensureComposerLock(event)(() => {
        refresh(event.getFile)
      })

    override def fileDeleted(event: VirtualFileEvent): Unit =
      ensureComposerLock(event)(() => {
        packages.remove(event.getFile)
      })

    override def fileMoved(event: VirtualFileMoveEvent): Unit = {}

    override def propertyChanged(event: VirtualFilePropertyEvent): Unit = {}

    override def fileCopied(event: VirtualFileCopyEvent): Unit = {}

    override def beforeContentsChange(event: VirtualFileEvent): Unit = {}

    override def beforeFileDeletion(event: VirtualFileEvent): Unit = {}

    override def beforeFileMovement(event: VirtualFileMoveEvent): Unit = {}

    override def beforePropertyChange(event: VirtualFilePropertyEvent): Unit = {}
  }
}

object InstalledPackages {
  def forFile(composerJsonFile: VirtualFile): Packages = {
    ApplicationManager.getApplication.getComponent(classOf[InstalledPackagesWatcher]).getPackages(composerJsonFile)
  }
}
