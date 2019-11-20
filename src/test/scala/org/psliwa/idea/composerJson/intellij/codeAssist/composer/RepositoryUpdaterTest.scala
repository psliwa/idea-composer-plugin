package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.openapi.application.ApplicationManager
import org.junit.Assert._
import org.psliwa.idea.composerJson.composer.model.PackageName
import org.psliwa.idea.composerJson.composer.model.repository.{Repository, RepositoryInfo, TestingRepositoryProvider}
import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest

class RepositoryUpdaterTest extends InspectionTest {

  def testGivenNotPackagistRepository_urlRepositoriesShouldBeEmpty(): Unit = {
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

  def testGivenComposerRepository_thereShouldBeOneUrlRepository(): Unit = {
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

    assertRepositories(List(url + "/packages.json"))
  }

  def testGivenPathRepository_givenPathIsRelative_thereShouldBeOneUrlRepository(): Unit = {
    val url = "some/relative/path"

    List("", "/") foreach { suffix =>
      checkInspection(
        s"""{
            |  "repositories": [
            |    {
            |      "type": "path",
            |      "url": "$url$suffix"
            |    }
            |  ]
            |}""".stripMargin
      )

      assertRepositories(List(myFixture.getFile.getVirtualFile.getParent.getUrl + s"/$url/composer.json"))
    }
  }

  def testGivenPathRepository_givenPathIsAbsolute_thereShouldBeOneUrlRepository(): Unit = {
    val url = "/some/relative/path"
    List("", "/") foreach { suffix =>
      checkInspection(
        s"""{
            |  "repositories": [
            |    {
            |      "type": "path",
            |      "url": "$url$suffix"
            |    }
            |  ]
            |}""".stripMargin
      )

      assertRepositories(List(s"file://$url/composer.json"))
    }
  }

  def testGivenExcludedPackagistRepo_thereShouldNotBeIncludedPackagistRepo(): Unit = {
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

  def testRepositoryInfoShouldBeClearedWhenRepositoriesPropertyWasRemoved(): Unit = {
    checkInspection(
      s"""{
         |  "repositories": [
         |    {
         |      "type": "composer",
         |      "url": "http://some-repo.org"
         |    }
         |  ]
         |}""".stripMargin
    )

    checkInspection(
      """
        |{}
      """.stripMargin
    )

    assertRepositories(List(), includePackagist = true)
  }

  def testGivenDirectlyIncludedPackagistRepo_thereShouldBeIncludedPackagistRepo(): Unit = {
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

  def testGivenInlinePackage_thereShouldBeInlinePackageInRepository(): Unit = {
    checkInspection(
      s"""{
         |  "repositories": [
         |    {
         |      "type": "package",
         |      "package": {
         |        "name": "inline/package",
         |        "version": "1.0.8"
         |      }
         |    }
         |  ]
         |}""".stripMargin
    )

    assertRepositories(List(), includePackagist = true, Map("inline/package" -> List("1.0.8")))
  }

  def testGivenIncompleteInlinePackage_repositoryShouldBeEmpty(): Unit = {
    checkInspection(
      s"""{
         |  "repositories": [
         |    {
         |      "type": "package",
         |      "package": {
         |        "version": "inline/package"
         |      }
         |    }
         |  ]
         |}""".stripMargin
    )

    assertTrue(getRepositoryInfo.isDefined)

    val packages = getRepositoryInfo.get.repository.map(_.getPackages).getOrElse(List())
    assertEquals(List(), packages)
  }

  def testGivenTwoInlinePackageVersions_thereShouldBeBothVersionsInRepository(): Unit = {
    checkInspection(
      s"""{
         |  "repositories": [
         |    {
         |      "type": "package",
         |      "package": {
         |        "name": "inline/package",
         |        "version": "1.0.8"
         |      }
         |    },
         |    {
         |      "type": "package",
         |      "package": {
         |        "name": "inline/package",
         |        "version": "1.0.9"
         |      }
         |    }
         |  ]
         |}""".stripMargin
    )

    assertRepositories(List(), includePackagist = true, Map("inline/package" -> List("1.0.8", "1.0.9")))
  }

  private def assertRepositories(expectedUrls: List[String],
                                 includePackagist: Boolean = true,
                                 expectedPackages: Map[String, List[String]] = Map()): Unit = {
    val repoInfo = getRepositoryInfo

    assertEquals(1, getRepositoryProvider.infos.size)
    assertTrue(repoInfo.isDefined)
    assertEquals(expectedUrls, repoInfo.get.urls)
    assertEquals(includePackagist, repoInfo.get.packagist)
    val repository = repoInfo.get.repository.getOrElse(Repository.inMemory[String](List()))
    assertRepository(repository, expectedPackages)
  }

  private def getRepositoryInfo: Option[RepositoryInfo] = {
    getRepositoryProvider.infos.get(myFixture.getFile.getVirtualFile.getCanonicalPath)
  }

  private def assertRepository[A](repository: Repository[A], expectedPackages: Map[String, List[String]]): Unit = {
    assertTrue(
      expectedPackages.forall {
        case (packageName, versions) =>
          repository.getPackages.contains(packageName) && versions.forall(
            repository.getPackageVersions(PackageName(packageName)).contains
          )
      }
    )
  }

  override def setUp(): Unit = {
    super.setUp()
    clearRepositories()
  }

  private def clearRepositories(): Unit = {
    getRepositoryProvider.infos.clear()
  }

  private def getRepositoryProvider: TestingRepositoryProvider = {
    Option(ApplicationManager.getApplication.getComponent(classOf[PackagesLoader]))
      .map(_.repositoryProviderFor(myFixture.getProject))
      .map(_.asInstanceOf[TestingRepositoryProvider])
      .get
  }

  override def tearDown(): Unit = {
    clearRepositories()
    super.tearDown()
  }
}
