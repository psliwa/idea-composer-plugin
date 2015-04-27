package org.psliwa.idea.composerJson.composer.repository

class RepositoryProviderWrapper[Package](
  repositoryProvider: RepositoryProvider[Package], 
  defaultRepository: Repository[Package],
  useDefaultRepository: String => Boolean
) extends RepositoryProvider[Package] {
  override def repositoryFor(file: String): Repository[Package] = {
    if(useDefaultRepository(file)) defaultRepository
    else repositoryProvider.repositoryFor(file)
  }
  override def updateRepository(file: String, info: RepositoryInfo) = repositoryProvider.updateRepository(file, info)
  override def hasDefaultRepository(file: String): Boolean = repositoryProvider.hasDefaultRepository(file)
}
