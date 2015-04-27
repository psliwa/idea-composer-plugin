package org.psliwa.idea.composerJson.composer.repository

import scala.collection.mutable

//RepositoryProvider only for testing
class TestingRepositoryProvider extends RepositoryProvider[Nothing] {

  val infos = mutable.Map[String, RepositoryInfo]()

  override def repositoryFor(file: String): Repository[Nothing] = EmptyRepository
  override def updateRepository(file: String, info: RepositoryInfo): Boolean = {
    val changed = infos.getOrElse(file, null) != info
    infos(file) = info

    changed
  }
  override def hasDefaultRepository(file: String): Boolean = true
}