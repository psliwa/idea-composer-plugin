package org.psliwa.idea.composerJson.composer.parsers

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.composer.model.{PackageDescriptor, Packages}

import scala.io.Source

class JsonParsersTest {
  @Test
  def parsePackageNames_givenValidJson_expectSomeList(): Unit = {
    val s =
      """
        |{
        |  "packageNames": [
        |    "ps\/image-optimizer",
        |    "ps\/fluent-traversable",
        |    "psliwa\/php-pdf"
        |  ]
        |}
      """.stripMargin

    val result = JsonParsers.parsePackageNames(s)

    assertFalse(result.isFailure)
    assertEquals(
      List("ps/image-optimizer", "ps/fluent-traversable", "psliwa/php-pdf"),
      result.get
    )
  }

  @Test
  def parsePackageNames_givenInvalidJson_expectedError(): Unit = {
    val s = "invalid json"

    val result = JsonParsers.parsePackageNames(s)
    assertTrue(result.isFailure)
  }
  @Test
  def parseVersions_givenValidJson_expectList(): Unit = {
    val json =
      """
        |{
        | "package": {
        |   "name": "ps/image-optimizer",
        |   "versions": {
        |     "dev-master": {},
        |     "1.0.0": {},
        |     "1.0.1": {}
        |   }
        | }
        |}
      """.stripMargin

    val result = JsonParsers.parseVersions(json)

    assertTrue(result.isSuccess)
    assertEquals(Set("dev-master", "1.0.0", "1.0.1"), result.get.toSet)
  }

  @Test
  def parseVersions_versionsAreMissing_expectError(): Unit = {
    val json =
      """
        |{
        | "package": {
        |   "name": "ps/image-optimizer"
        | }
        |}
      """.stripMargin

    val result = JsonParsers.parseVersions(json)

    assertTrue(result.isFailure)
  }

  @Test
  def parseVersions_givenInvalidJson_expectError(): Unit = {
    val json = "invalid json"

    val result = JsonParsers.parseVersions(json)

    assertTrue(result.isFailure)
  }

  @Test
  def parseLockPackages_givenValidJson_expectList(): Unit = {
    List(true, false).foreach(dev => {
      val packagesKey = "packages" + (if (dev) "-dev" else "")
      val json =
        s"""
          |{
          |  "$packagesKey": [
          |    {
          |      "name": "ps/image-optimizer",
          |      "version": "1.0.0"
          |    },
          |    {
          |      "name": "ps/fluent-traversable",
          |      "version": "0.3.0",
          |      "homepage": "url"
          |    }
          |  ]
          |}
        """.stripMargin

      val result = JsonParsers.parseLockPackages(json)

      assertEquals(packagesKey,
                   Packages(PackageDescriptor("ps/image-optimizer", "1.0.0", dev),
                            PackageDescriptor("ps/fluent-traversable", "0.3.0", dev, Some("url"))),
                   result)
    })
  }

  @Test
  def parseLockPackages_givenInvalidJson_expectError(): Unit = {
    val json = "invalid json"

    val result = JsonParsers.parseLockPackages(json)

    assertTrue(result.isEmpty)
  }

  @Test
  def parseRealComposerLockFile(): Unit = {
    val composerLockContent: String = readFileFromClasspath("composer.lock")

    val result = JsonParsers.parseLockPackages(composerLockContent)

    assertTrue(result.nonEmpty)
  }

  private def readFileFromClasspath(filepath: String): String = {
    val source = Source.fromURL(this.getClass.getResource(s"/org/psliwa/idea/composerJson/$filepath"), "UTF-8")
    val fileContent: String = source.mkString
    source.close()

    fileContent
  }

  @Test
  def parsePackages_givenValidJson_expectPackagesWithVersions(): Unit = {
    val json =
      """
        |{
        |  "packages": {
        |    "some/package": {
        |      "1.0.0": {},
        |      "2.0.0": {}
        |    },
        |    "some/package2": {
        |      "3.0.0": {}
        |    }
        |  }
        |}
      """.stripMargin

    val result = JsonParsers.parsePackages(json)

    assertTrue(result.isSuccess)
    assertEquals(RepositoryPackages(Map(
                                      "some/package" -> Seq("1.0.0", "2.0.0"),
                                      "some/package2" -> Seq("3.0.0")
                                    ),
                                    List()),
                 result.get)
  }

  @Test
  def parsePackages_givenInvalidJson_expectError(): Unit = {
    val json =
      """
        |{
        |}
      """.stripMargin

    val result = JsonParsers.parsePackages(json)

    assertTrue(result.isFailure)
  }

  @Test
  def parsePackages_givenIncludesInJson_expectIncludesInResult(): Unit = {
    val json =
      """
        |{
        |  "packages": {},
        |  "includes": {
        |    "some-include.json": {}
        |  }
        |}
      """.stripMargin

    val result = JsonParsers.parsePackages(json)

    assertTrue(result.isSuccess)
    assertEquals(RepositoryPackages(Map(), List("some-include.json")), result.get)
  }

  @Test
  def parsePackages_givenPackagesAsArray_givenIncludes_expectIncludesInResult(): Unit = {
    val json =
      """
        |{
        |  "packages": [],
        |  "includes": {
        |    "some-include.json": {}
        |  }
        |}
      """.stripMargin

    val result = JsonParsers.parsePackages(json)

    assertTrue(result.isSuccess)
    assertEquals(RepositoryPackages(Map(), List("some-include.json")), result.get)
  }

  @Test
  def parsePackageFromComposerJson_givenValidPackage_expectPackageAndVersion(): Unit = {
    val json =
      """
        |{
        |  "name": "some/pkg",
        |  "version": "1.0.0"
        |}
      """.stripMargin

    val result = JsonParsers.parsePackages(json)

    assertTrue(result.isSuccess)
    assertEquals(RepositoryPackages(Map("some/pkg" -> Seq("1.0.0")), List.empty), result.get)
  }

  @Test
  def parsePackageFromComposerJson_givenValidPackage_versionIsMissing_expectPackage(): Unit = {
    val json =
      """
        |{
        |  "name": "some/pkg"
        |}
      """.stripMargin

    val result = JsonParsers.parsePackages(json)

    assertTrue(result.isSuccess)
    assertEquals(RepositoryPackages(Map("some/pkg" -> Seq()), List.empty), result.get)
  }
}
