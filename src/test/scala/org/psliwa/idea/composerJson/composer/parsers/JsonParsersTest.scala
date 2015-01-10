package org.psliwa.idea.composerJson.composer.parsers

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.composer.Package

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
  def parsePackages_givenValidJson_expectList() = {
    val json =
      """
        |{
        |  "packages": [
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

    val result = JsonParsers.parsePackages(json)

    assertTrue(result.isRight)
    assertEquals(List(Package("ps/image-optimizer", "1.0.0"), Package("ps/fluent-traversable", "0.3.0")), result.right.get)
  }

  @Test
  def parsePackages_givenInvalidJson_expectError() = {
    val json = "invalid json"

    val result = JsonParsers.parsePackages(json)

    assertTrue(result.isLeft)
  }
}
