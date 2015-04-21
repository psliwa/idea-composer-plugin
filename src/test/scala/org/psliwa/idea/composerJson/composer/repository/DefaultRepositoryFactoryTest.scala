package org.psliwa.idea.composerJson.composer.repository

import org.junit.Assert._
import org.junit.Test

import DefaultRepositoryProvider._

class DefaultRepositoryFactoryTest {

  val packagistRepository = new FakeRepository(List("packagist"))

  @Test
  def givenFewUrls_createRepositoryFromFewUrls(): Unit = {

    //given

    val factory = new DefaultRepositoryFactory(url => new FakeRepository(List(url)), packagistRepository)

    //when

    val repository = factory.repositoryFor(new RepositoryInfo(List("url1", "url2"), false))
    Set().toSeq
    //then

    assertEquals(List("url1", "url2"), repository.getPackages)
  }

  @Test
  def givenPackagistRepository_createdRepositoryShouldContainsAlsoPackagistRepo(): Unit = {
    //given

    val factory = new DefaultRepositoryFactory(url => new FakeRepository(List(url)), packagistRepository)

    //when

    val repository = factory.repositoryFor(new RepositoryInfo(List(), true))

    //then

    assertEquals(packagistRepository.getPackages, repository.getPackages)
  }
}
