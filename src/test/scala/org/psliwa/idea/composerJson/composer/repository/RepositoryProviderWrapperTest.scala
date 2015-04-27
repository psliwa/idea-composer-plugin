package org.psliwa.idea.composerJson.composer.repository

import org.junit.Assert._
import org.junit.Test

class RepositoryProviderWrapperTest {
  val innerRepository = new InMemoryRepository[String](List("package1"))
  val innerRepositoryProvider = new RepositoryProvider[String] {
    override def repositoryFor(file: String): Repository[String] = innerRepository
    override def updateRepository(file: String, info: RepositoryInfo) = false
    override def hasDefaultRepository(file: String): Boolean = false
  }

  val defaultRepository = new InMemoryRepository[String](List("package2"))

  @Test
  def defaultRepositoryShouldBeReturnedWhenPredicateIsTrue() = {
    //given

    val defaultFile = "defaultFile"
    val predicate = (file: String) => file == defaultFile

    val repositoryProvider = new RepositoryProviderWrapper[String](innerRepositoryProvider, defaultRepository, predicate)

    //when & then

    assertEquals(defaultRepository, repositoryProvider.repositoryFor(defaultFile))
    assertEquals(innerRepository, repositoryProvider.repositoryFor("different-file"))
  }
}
