package org.psliwa.idea.composerJson.composer

import org.junit.Assert._
import org.junit.Test

class LoadPackagesTest {
  @Test
  def loadFromString_givenValidJson_expectSomeList() = {
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

    val result = Packagist.loadPackagesFromString(s)

    assertFalse(result.isLeft)
    assertEquals(
      List("ps/image-optimizer", "ps/fluent-traversable", "psliwa/php-pdf"),
      result.right.get
    )
  }

  @Test
  def loadFromString_givenInvalidJson_expectedError() = {
    val s = "invalid json"

    val result = Packagist.loadPackagesFromString(s)
    assertTrue(result.isLeft)
  }

  @Test
  def loadJsonFromPackagist_shouldBeLoaded() = {
    val result = Packagist.loadPackagesFromPackagist()

    assertFalse(result.isLeft)
    assertTrue(result.right.toOption.map(_.contains("packageNames")).get)
  }

  @Test
  def loadJsonFromPackagist_givenInvalidIri_expectedError() = {
    val result = Packagist.loadUri("some/invalid/uri.json")

    assertTrue(result.isLeft)
  }
}
