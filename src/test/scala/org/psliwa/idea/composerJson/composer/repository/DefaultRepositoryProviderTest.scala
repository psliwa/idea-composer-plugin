package org.psliwa.idea.composerJson.composer.repository

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.composer.repository.DefaultRepositoryProviderTest._

class DefaultRepositoryProviderTest {
  val repositoryFactory = new FakeRepositoryFactory()
  val provider = new DefaultRepositoryProvider(repositoryFactory)

  @Test
  def givenRepositoryInfoForFile_repositoryShouldBeCreatedByFactory(): Unit = {

    //given

    val file = "someFile"
    val repositoryInfo = new RepositoryInfo(List("a"), false)
    val repository = new FakeRepository(List())

    repositoryFactory.setRepositories(repositoryInfo, repository)
    provider.updateRepository(file, repositoryInfo)

    //when

    val actualRepository = provider.repositoryFor(file)

    //then

    assertEquals(repository, actualRepository)
  }

  @Test
  def givenChangedRepositoryInfoForFile_changeRepositoryForFile(): Unit = {

    //given

    val file = "someFile"
    val repositoryInfo = new RepositoryInfo(List("a"), false)
    val changedRepositoryInfo = new RepositoryInfo(List("b"), false)
    val repository = new FakeRepository(List("package1"))
    val changedRepository = new FakeRepository(List("package2"))

    repositoryFactory.setRepositories(repositoryInfo, repository)
    repositoryFactory.setRepositories(changedRepositoryInfo, changedRepository)

    provider.updateRepository(file, repositoryInfo)
    provider.repositoryFor(file)
    provider.updateRepository(file, changedRepositoryInfo)

    //when

    val actualRepository = provider.repositoryFor(file)

    //then

    assertEquals(changedRepository, actualRepository)
  }
}

object DefaultRepositoryProviderTest {
  class FakeRepositoryFactory extends DefaultRepositoryProvider.RepositoryFactory[String] {
    private var repositories = Map[RepositoryInfo, Repository[String]]()

    override def repositoryFor(repositoryInfo: RepositoryInfo): Repository[String] = repositories.get(repositoryInfo).get

    def setRepositories(repositoryInfo: RepositoryInfo, repository: Repository[String]) = {
      repositories += (repositoryInfo -> repository)
    }
  }
}
