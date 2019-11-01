package org.psliwa.idea.composerJson.composer.repository

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.composer.repository.DefaultRepositoryProvider._

class DefaultRepositoryFactoryTest {

  val packagistRepository = Repository.inMemory(List("packagist"))
  val factory = new DefaultRepositoryFactory(url => new InMemoryRepository(List(url)), packagistRepository, pkg => pkg)

  @Test
  def givenFewUrls_createRepositoryFromFewUrls(): Unit = {

    //when

    val repository = factory.repositoryFor(RepositoryInfo(List("url1", "url2"), false))

    //then

    assertEquals(List("url1", "url2"), repository.getPackages)
  }

  @Test
  def givenPackagistRepository_createdRepositoryShouldContainsAlsoPackagistRepo(): Unit = {

    //when

    val repository = factory.repositoryFor(new RepositoryInfo(List(), true))

    //then

    assertEquals(packagistRepository.getPackages, repository.getPackages)
  }

  @Test
  def givenRepository_createdRepositoryShouldContainsGivenOne(): Unit = {
    //given

    val packageName = "vendor/pkg"
    val packages = List(packageName)
    val versions = Map(packageName -> List("1.0.0"))

    //when

    val repository = factory.repositoryFor(new RepositoryInfo(List(), false, Some(new InMemoryRepository[String](packages, versions))))

    //then

    assertEquals(packages, repository.getPackages)
    assertEquals(versions.getOrElse(packageName, List()), repository.getPackageVersions(packageName))
  }
}
