package org.psliwa.idea.composerJson.composer.parsers

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.composer.{Package, Packages}

class JsonParsersTest {
  @Test
  def parsePackageNames_givenValidJson_expectSomeList() = {
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

    assertFalse(result.isLeft)
    assertEquals(
      List("ps/image-optimizer", "ps/fluent-traversable", "psliwa/php-pdf"),
      result.right.get
    )
  }

  @Test
  def parsePackageNames_givenInvalidJson_expectedError() = {
    val s = "invalid json"

    val result = JsonParsers.parsePackageNames(s)
    assertTrue(result.isLeft)
  }


  @Test
  def parseVersions_givenValidJson_expectList() = {
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

    assertTrue(result.isRight)
    assertEquals(List("dev-master", "1.0.0", "1.0.1"), result.right.get)
  }

  @Test
  def parseVersions_versionsAreMissing_expectError() = {
    val json =
      """
        |{
        | "package": {
        |   "name": "ps/image-optimizer"
        | }
        |}
      """.stripMargin

    val result = JsonParsers.parseVersions(json)

    assertTrue(result.isLeft)
  }

  @Test
  def parseVersions_givenInvalidJson_expectError() = {
    val json = "invalid json"

    val result = JsonParsers.parseVersions(json)

    assertTrue(result.isLeft)
  }

  @Test
  def parseLockPackages_givenValidJson_expectList() = {
    List(true, false).foreach(dev => {
      val packagesKey = "packages"+(if(dev) "-dev" else "")
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
          |      "version": "0.3.0"
          |    }
          |  ]
          |}
        """.stripMargin

      val result = JsonParsers.parseLockPackages(json)

      assertTrue(packagesKey, result.isRight)
      assertEquals(packagesKey, Packages(Package("ps/image-optimizer", "1.0.0", dev), Package("ps/fluent-traversable", "0.3.0", dev)), result.right.get)
    })
  }

  @Test
  def parseLockPackages_givenInvalidJson_expectError() = {
    val json = "invalid json"

    val result = JsonParsers.parseLockPackages(json)

    assertTrue(result.isLeft)
  }

  @Test
  def parsePackages_givenValidJson_expectPackagesWithVersions() = {
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

    assertTrue(result.isRight)
    assertEquals(RepositoryPackages(Map(
        "some/package" -> Seq("1.0.0", "2.0.0"),
        "some/package2" -> Seq("3.0.0")
      ), List()),
      result.right.get
    )
  }

  @Test
  def parsePackages_givenInvalidJson_expectError() = {
    val json =
      """
        |{
        |}
      """.stripMargin

    val result = JsonParsers.parsePackages(json)

    assertTrue(result.isLeft)
  }

  @Test
  def parsePackages_givenIncludesInJson_expectIncludesInResult() = {
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

    assertTrue(result.isRight)
    assertEquals(RepositoryPackages(Map(), List("some-include.json")), result.right.get)
  }
}
