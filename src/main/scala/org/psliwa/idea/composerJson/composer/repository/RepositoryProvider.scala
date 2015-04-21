package org.psliwa.idea.composerJson.composer.repository

trait RepositoryProvider[Package] {
  def repositoryFor(file: String): Repository[Package]
  def updateRepository(file: String, info: RepositoryInfo)
}
