package org.psliwa.idea.composerJson.composer.repository

import org.junit.Assert._
import org.junit.Test

class LoadVersionsTest {

  @Test
  def loadFromString_givenValidJson_expectList() = {
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

    val result = Packagist.loadVersionsFromString(json)

    assertTrue(result.isRight)
    assertEquals(List("dev-master", "1.0.0", "1.0.1"), result.right.get)
  }

  @Test
  def loadFromString_versionsAreMissing_expectError() = {
    val json =
      """
        |{
        | "package": {
        |   "name": "ps/image-optimizer"
        | }
        |}
      """.stripMargin

    val result = Packagist.loadVersionsFromString(json)

    assertTrue(result.isLeft)
  }

  @Test
  def loadFromString_givenInvalidJson_expectError() = {
    val json = "invalid json"

    val result = Packagist.loadVersionsFromString(json)

    assertTrue(result.isLeft)
  }

  @Test
  def loadFromPackagist_givenValidPackage_expectSomeVersions() = {
    val result = Packagist.loadVersions("symfony/symfony")

    assertTrue(result.isRight)
    assertTrue(result.right.get.size > 0)
  }

  @Test
  def loadFromPackagist_givenInvalidPackage_expectError() = {
    val result = Packagist.loadVersions("some-unexisting-vendor/some-unexisting-package-123")

    assertTrue(result.isLeft)
  }
}
