package org.psliwa.idea.composerJson.composer.repository

import org.junit.Assert._
import org.junit.Test

class LoadPackagesTest {

  @Test
  def loadJsonFromPackagist_shouldBeLoaded(): Unit = {
    val result = Packagist.loadPackagesFromPackagist(Packagist.defaultUrl)

    assertFalse(result.isFailure)
    assertTrue(result.toOption.map(_.contains("packageNames")).get)
  }

  @Test
  def loadJsonFromPackagist_givenInvalidIri_expectedError(): Unit = {
    val result = Packagist.loadUri(Packagist.defaultUrl)("some/invalid/uri.json")

    assertTrue(result.isFailure)
  }
}
