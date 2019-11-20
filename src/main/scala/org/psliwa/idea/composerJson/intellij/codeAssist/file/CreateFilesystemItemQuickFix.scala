package org.psliwa.idea.composerJson.intellij.codeAssist.file

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.notification.{Notification, NotificationType, Notifications}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.psi.{PsiDirectory, PsiElement, PsiFile, PsiFileSystemItem}
import com.intellij.util.IncorrectOperationException
import org.psliwa.idea.composerJson.ComposerBundle
import scala.jdk.CollectionConverters._

import scala.annotation.tailrec

private object CreateFilesystemItemQuickFix {

  def file(element: JsonStringLiteral): LocalQuickFixOnPsiElement = new CreateFileQuickFix(element)
  def directory(element: JsonStringLiteral): LocalQuickFixOnPsiElement = new CreateDirectoryQuickFix(element)

  private def tryCreateDir(dir: PsiDirectory, path: String): Either[String, PsiDirectory] = {
    try {
      Right(dir.createSubdirectory(path))
    } catch {
      case e: IncorrectOperationException => Left(e.getMessage)
    }
  }

  private def tryCreateFile(dir: PsiDirectory, path: String): Either[String, PsiFile] = {
    try {
      Right(dir.createFile(path))
    } catch {
      case e: IncorrectOperationException => Left(e.getMessage)
    }
  }

  private class CreateDirectoryQuickFix(element: JsonStringLiteral) extends CreateFilesystemItemQuickFix(element) {
    override def getText = ComposerBundle.message("inspection.quickfix.createDirectory", getPath)

    override def createLeafItem(directory: PsiDirectory, s: String): Either[String, PsiFileSystemItem] =
      tryCreateDir(directory, s)
  }

  private class CreateFileQuickFix(element: JsonStringLiteral) extends CreateFilesystemItemQuickFix(element) {
    override def getText = ComposerBundle.message("inspection.quickfix.createFile", getPath)
    override def createLeafItem(directory: PsiDirectory, s: String): Either[String, PsiFileSystemItem] =
      tryCreateFile(directory, s)
  }

  abstract private class CreateFilesystemItemQuickFix(element: JsonStringLiteral)
      extends LocalQuickFixOnPsiElement(element) {

    private val errorNotificationDisplayGroupId = "error"

    def createLeafItem(directory: PsiDirectory, s: String): Either[String, PsiFileSystemItem]

    override def invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement): Unit = {
      val dir = file.getContainingDirectory

      @tailrec
      def createFilePath(dir: PsiDirectory, paths: List[String]): Either[String, PsiFileSystemItem] = {
        paths match {
          case h :: Nil => createLeafItem(dir, h)
          case ".." :: t =>
            Option(dir.getParent) match {
              case Some(parent) => createFilePath(parent, t)
              case None => Left(ComposerBundle.message("inspection.quickfix.filepathCannotBeCreated", getPath))
            }
          case "." :: t => createFilePath(dir, t)
          case h :: t => {
            val subDir = Option(dir.findSubdirectory(h))
              .map(Right(_))
              .getOrElse(tryCreateDir(dir, h))

            subDir match {
              case Right(x) => createFilePath(x, t)
              case _ => subDir
            }
          }
          case Nil => Right(dir)
        }
      }

      createFilePath(dir, getPath.split("/").filter(!_.isEmpty).toList).left
        .map(notifyError)
        .right
        .filter(_ => !isUnitTestMode)
        .foreach(_.right.foreach(navigateTo))
    }

    private def isUnitTestMode = ApplicationManager.getApplication.isUnitTestMode

    private def notifyError(error: String) = {
      Notifications.Bus.notify(
        new Notification(
          errorNotificationDisplayGroupId,
          ComposerBundle.message("error.directoryCreationError"),
          error,
          NotificationType.ERROR
        )
      )
    }

    private def navigateTo(item: PsiFileSystemItem): Unit = {
      ApplicationManager.getApplication.invokeLater(() => item.navigate(false))
    }

    override def getFamilyName: String = ComposerBundle.message("inspection.group")

    protected def getPath = element.getTextFragments.asScala.map(_.second).mkString("")
  }
}
