package org.psliwa.idea.composerJson.composer.repository

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.composer.model.repository.{InMemoryRepository, Repository, RepositoryInfo}
import org.psliwa.idea.composerJson.composer.repository.DefaultRepositoryProviderTest.FakeRepositoryFactory

class DefaultRepositoryProviderTest {
  val repositoryFactory = new FakeRepositoryFactory()
  val defaultRepository: Repository[String] = Repository.inMemory(List("some/package123321"))
  val provider = new DefaultRepositoryProvider(repositoryFactory, defaultRepository)

  @Test
  def givenRepositoryInfoForFile_repositoryShouldBeCreatedByFactory(): Unit = {

    //given

    val file = "someFile"
    val repositoryInfo = RepositoryInfo(List("a"), packagist = false)
    val repository = Repository.inMemory[String](List())

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
    val repository = Repository.inMemory(List("package1"))
    val changedRepository = Repository.inMemory(List("package2"))

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

  @Test
  def givenFileHasNotRepositoryInfo_defaultRepositoryShouldBeUsed() = {
    val repository = provider.repositoryFor("some-file")

    assertEquals(defaultRepository, repository)
  }

  @Test
  def givenFileHasRepositoryInfoOnlyWithPackagistRepo_itShouldHasDefaultRepository() = {
    //given

    val file = "file"
    provider.updateRepository(file, new RepositoryInfo(List(), true))

    //when & then

    assertTrue(provider.hasDefaultRepository(file))
  }

  @Test
  def givenFileHasNotRepositoryInfo_itShouldToHaveDefaultRepository() = {
    assertTrue(provider.hasDefaultRepository("some-file"))
  }

  @Test
  def givenFileHasPackagistRepoTurnedOff_itShouldNotToHaveDefaultRepository() = {
    //given

    val file = "file"
    provider.updateRepository(file, new RepositoryInfo(List(), false))

    //when & then

    assertFalse(provider.hasDefaultRepository(file))
  }

  @Test
  def givenFileHasPackagistRepoTurnedOff_packagistRepoIsSpecifiedAsCustormRepo_isShouldHaveDefaultRepository() = {
    //given

    val file = "file"
    provider.updateRepository(file, new RepositoryInfo(List("https://packagist.org/"), false))

    //when & then

    assertTrue(provider.hasDefaultRepository(file))
  }

  @Test
  def givenFileHasFewUrlsInRepositoryInfo_itShouldNotToHaveDefaultRepository() = {
    //given

    val file = "file"
    provider.updateRepository(file, new RepositoryInfo(List("url1"), true))

    //when & then

    assertFalse(provider.hasDefaultRepository(file))
  }

  @Test
  def givenFileHasNotRepositoryInfo_updateRepositoryInfo_trueShouldBeReturnedToIndicateChange() = {
    val changed = provider.updateRepository("file", new RepositoryInfo(List("url1"), true))

    assertTrue(changed)
  }

  @Test
  def givenFileHasRepositoryInfo_updateRepositoryInfoWithTheSameObject_falseShouldBeReturnedToIndicateNoChange() = {
    //given

    val file = "file"
    val repoInfo = new RepositoryInfo(List("url1"), true)

    provider.updateRepository(file, repoInfo)

    //when

    val changed = provider.updateRepository(file, repoInfo.copy())

    //then

    assertFalse(changed)
  }

  @Test
  def givenFileHasRepositoryInfo_updateRepositoryInfoWithDifferentObject_trueShouldBeReturned() = {
    //given

    val file = "file"
    val repoInfo = new RepositoryInfo(List("url1"), true)

    provider.updateRepository(file, repoInfo)

    //when

    val changed = provider.updateRepository(file, repoInfo.copy(urls = List()))

    //then

    assertTrue(changed)
  }

  @Test
  def givenFileHasRepositoryInfo_repositoryFactoryShouldBeCalledOnlyOnce() = {
    //given

    val file = "someFile"
    val repositoryInfo = RepositoryInfo(List("a"), packagist = false)
    val repository = Repository.inMemory[String](List())

    repositoryFactory.setRepositories(repositoryInfo, repository)
    provider.updateRepository(file, repositoryInfo)

    //when

    provider.repositoryFor(file)
    provider.updateRepository(file, repositoryInfo)
    provider.repositoryFor(file)

    //then

    assertEquals(1, repositoryFactory.callsFor(repositoryInfo))
  }
}

object DefaultRepositoryProviderTest {
  class FakeRepositoryFactory extends DefaultRepositoryProvider.RepositoryFactory[String] {
    private var repositories = Map[RepositoryInfo, Repository[String]]()
    private var calls = Map[RepositoryInfo, Int]()

    override def repositoryFor(repositoryInfo: RepositoryInfo): Repository[String] = {
      calls += repositoryInfo -> (callsFor(repositoryInfo) + 1)
      repositories(repositoryInfo)
    }

    def callsFor(repositoryInfo: RepositoryInfo): Int = calls.getOrElse(repositoryInfo, 0)

    def setRepositories(repositoryInfo: RepositoryInfo, repository: Repository[String]) = {
      repositories += (repositoryInfo -> repository)
    }
  }
}
