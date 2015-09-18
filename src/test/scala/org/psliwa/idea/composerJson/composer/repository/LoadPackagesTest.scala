package org.psliwa.idea.composerJson.composer.repository

import org.junit.Assert._
import org.junit.Test

class LoadPackagesTest {

  @Test
  def loadJsonFromPackagist_shouldBeLoaded() = {
    val result = Packagist.loadPackagesFromPackagist()

    assertFalse(result.isFailure)
    assertTrue(result.toOption.map(_.contains("packageNames")).get)
  }

  @Test
  def loadJsonFromPackagist_givenInvalidIri_expectedError() = {
    val result = Packagist.loadUri("some/invalid/uri.json")

    assertTrue(result.isFailure)
  }
}
