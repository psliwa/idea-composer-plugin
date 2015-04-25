package org.psliwa.idea.composerJson.composer.repository

import scala.collection.mutable

//RepositoryProvider only for testing
class TestingRepositoryProvider extends RepositoryProvider[Nothing] {

  val infos = mutable.Map[String, RepositoryInfo]()

  override def repositoryFor(file: String): Repository[Nothing] = EmptyRepository
  override def updateRepository(file: String, info: RepositoryInfo): Unit = {
    infos(file) = info
  }
}
