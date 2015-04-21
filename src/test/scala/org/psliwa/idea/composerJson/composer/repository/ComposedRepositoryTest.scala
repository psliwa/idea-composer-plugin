package org.psliwa.idea.composerJson.composer.repository

import org.junit.Assert._
import org.junit.Test

class ComposedRepositoryTest {

  @Test
  def givenFewRepositories_composedRepositoryShouldMergePackagesFromAllRepos(): Unit = {
    //given

    val repository = new ComposedRepository(
      List(
        new FakeRepository(List("package1", "package2")),
        new FakeRepository(List("package3", "package4"))
      )
    )

    //when

    val packages = repository.getPackages

    //then

    assertEquals(List("package1", "package2", "package3", "package4"), packages)
  }

  @Test
  def givenFewRepositories_composedRepositoryShouldMergeVersionsFromAllRepos(): Unit = {

    //given

    val repository = new ComposedRepository(
      List(
        new FakeRepository(List("package1", "package2"), Map("package1" -> List("1.0.0"), "package2" -> List("2.0.0"))),
        new FakeRepository(List("package1", "package2"), Map("package1" -> List("1.0.1"), "package2" -> List("2.0.1")))
      )
    )

    //when

    val versions = repository.getPackageVersions("package1")

    //then

    assertEquals(List("1.0.0", "1.0.1"), versions)
  }

}
