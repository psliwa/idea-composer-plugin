package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.openapi.application.ApplicationManager
import org.psliwa.idea.composerJson.composer.repository.TestingRepositoryProvider
import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest
import org.junit.Assert._

class RepositoryUpdaterTest extends InspectionTest {

  def testGivenNotPackagistRepository_urlRepositoriesShouldBeEmpty() = {
    val url = "https://github.com/foobar/intermediate.git"
    checkInspection(
      s"""{
        |  "repositories": [
        |    {
        |      "type": "git",
        |      "url": "$url"
        |    }
        |  ]
        |}""".stripMargin
    )

    assertRepositories(List())
  }

  def testGivenComposerRepository_thereShouldBeOneUrlRepository() = {
    val url = "https://github.com/foobar/intermediate.git"
    checkInspection(
      s"""{
         |  "repositories": [
         |    {
         |      "type": "composer",
         |      "url": "$url"
         |    }
         |  ]
         |}""".stripMargin
    )

    assertRepositories(List(url+"/packages.json"))
  }

  def testGivenExcludedPackagistRepo_thereShouldNotBeIncludedPackagistRepo() = {
    checkInspection(
      s"""{
         |  "repositories": [
         |    {
         |      "packagist": false
         |    }
         |  ]
         |}""".stripMargin
    )

    assertRepositories(List(), includePackagist = false)
  }

  def testGivenDirectlyIncludedPackagistRepo_thereShouldBeIncludedPackagistRepo() = {
    checkInspection(
      s"""{
         |  "repositories": [
         |    {
         |      "packagist": true
         |    }
         |  ]
         |}""".stripMargin
    )

    assertRepositories(List(), includePackagist = true)
  }

  private def assertRepositories(expectedUrls: List[String], includePackagist: Boolean = true): Unit = {
    val infos = getRepositoryProvider.infos.get(myFixture.getFile.getVirtualFile.getCanonicalPath)

    assertEquals(1, getRepositoryProvider.infos.size)
    assertTrue(infos.isDefined)
    assertEquals(expectedUrls, infos.get.urls)
    assertEquals(includePackagist, infos.get.packagist)
  }

  override def setUp() = {
    super.setUp()
    clearRepositories()
  }

  private def clearRepositories() = {
    getRepositoryProvider.infos.clear()
  }

  private def getRepositoryProvider: TestingRepositoryProvider = {
    Option(ApplicationManager.getApplication.getComponent(classOf[PackagesLoader]))
      .map(_.repositoryProvider)
      .map(_.asInstanceOf[TestingRepositoryProvider])
      .get
  }

  override def tearDown(): Unit = {
    super.tearDown()
    clearRepositories()
  }
}
