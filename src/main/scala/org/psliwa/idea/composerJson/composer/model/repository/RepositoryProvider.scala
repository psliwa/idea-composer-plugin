package org.psliwa.idea.composerJson.composer.model.repository

trait RepositoryProvider[Package] {
  def repositoryFor(file: String): Repository[Package]

  /**
    * @return Return true when repositoryInfo was changed, false otherwise
    */
  def updateRepository(file: String, info: RepositoryInfo): Boolean
  def hasDefaultRepository(file: String): Boolean
}

object EmptyRepositoryProvider extends RepositoryProvider[Nothing] {
  override def repositoryFor(file: String): Repository[Nothing] = EmptyRepository
  override def updateRepository(file: String, info: RepositoryInfo): Boolean = false
  override def hasDefaultRepository(file: String): Boolean = true
}
