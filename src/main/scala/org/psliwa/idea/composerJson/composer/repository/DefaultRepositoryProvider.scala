package org.psliwa.idea.composerJson.composer.repository

import DefaultRepositoryProvider.RepositoryFactory
import scala.collection.mutable

class DefaultRepositoryProvider[Package](repositoryFactory: RepositoryFactory[Package]) extends RepositoryProvider[Package] {
  private val repositories = mutable.Map[String, Repository[Package]]()
  private val infos = mutable.Map[String, RepositoryInfo]()

  override def repositoryFor(file: String): Repository[Package] = {
    repositories.getOrElse(file, getRepositoryFromFactory(file))
  }
  override def updateRepository(file: String, info: RepositoryInfo): Unit = {
    infos(file) = info
    repositories -= file
  }

  private def getRepositoryFromFactory(file: String): Repository[Package] = {
    val repository = infos
      .get(file)
      .map(repositoryFactory.repositoryFor)
      .get

    repositories(file) = repository

    repository
  }
}

object DefaultRepositoryProvider {
  trait RepositoryFactory[Package] {
    def repositoryFor(repositoryInfo: RepositoryInfo): Repository[Package]
  }

  class DefaultRepositoryFactory[Package](
    repositoryFromUrl: String => Repository[Package],
    packagistRepository: Repository[Package]
  ) extends RepositoryFactory[Package] {
    override def repositoryFor(repositoryInfo: RepositoryInfo): Repository[Package] = {
      new ComposedRepository(
        repositoryInfo.urls.map(repositoryFromUrl) ++ List(packagistRepository).filter(_ => repositoryInfo.packagist)
      )
    }
  }
}
