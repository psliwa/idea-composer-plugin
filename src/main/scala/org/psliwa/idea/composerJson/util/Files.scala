package org.psliwa.idea.composerJson.util

import com.intellij.psi.{PsiDirectory, PsiFile, PsiFileSystemItem}

import scala.annotation.tailrec

object Files {
  def findDir(rootDir: PsiDirectory, path: String): Option[PsiDirectory] = findPath(rootDir, path) match {
    case Some(x: PsiDirectory) => Some(x)
    case _ => None
  }

  def findPath(rootDir: PsiDirectory, path: String): Option[PsiFileSystemItem] = {
    @tailrec
    def loop(rootDir: PsiDirectory, paths: List[String]): Option[PsiFileSystemItem] = {
      paths match {
        case Nil => Some(rootDir)
        case ".." :: t =>
          Option(rootDir.getParent) match {
            case Some(parent) => loop(parent, t)
            case None => None
          }
        case "." :: t => loop(rootDir, t)
        case h :: t =>
          val subPath = Option(rootDir.findSubdirectory(h))
            .orElse(Option(rootDir.findFile(h)))

          subPath match {
            case Some(x: PsiDirectory) => loop(x, t)
            case Some(x: PsiFile) if t.isEmpty => Some(x)
            case _ => None
          }
      }
    }

    loop(rootDir, path.split("/").toList.filter(!_.isEmpty))
  }

  def findFile(rootDir: PsiDirectory, path: String): Option[PsiFile] = {
    findPath(rootDir, path) match {
      case Some(x: PsiFile) => Some(x)
      case _ => None
    }
  }
}
