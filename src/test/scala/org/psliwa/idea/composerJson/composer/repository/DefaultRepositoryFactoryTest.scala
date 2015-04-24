package org.psliwa.idea.composerJson.composer.repository

import org.junit.Assert._
import org.junit.Test

import DefaultRepositoryProvider._

class DefaultRepositoryFactoryTest {

  val packagistRepository = new InMemoryRepository(List("packagist"))

  @Test
  def givenFewUrls_createRepositoryFromFewUrls(): Unit = {

    //given

    val factory = new DefaultRepositoryFactory(url => new InMemoryRepository(List(url)), packagistRepository, pkg => pkg)

    //when

    val repository = factory.repositoryFor(new RepositoryInfo(List("url1", "url2"), false))
    Set().toSeq
    //then

    assertEquals(List("url1", "url2"), repository.getPackages)
  }

  @Test
  def givenPackagistRepository_createdRepositoryShouldContainsAlsoPackagistRepo(): Unit = {
    //given

    val factory = new DefaultRepositoryFactory(url => new InMemoryRepository(List(url)), packagistRepository, pkg => pkg)

    //when

    val repository = factory.repositoryFor(new RepositoryInfo(List(), true))

    //then

    assertEquals(packagistRepository.getPackages, repository.getPackages)
  }
}
