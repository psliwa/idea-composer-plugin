package org.psliwa.idea.composerJson.composer.repository

import org.junit.Assert._
import org.junit.Test

class LoadVersionsTest {

  @Test
  def loadFromPackagist_givenValidPackage_expectSomeVersions() = {
    val result = Packagist.loadVersions("symfony/symfony")

    assertTrue(result.isSuccess)
    assertTrue(result.get.nonEmpty)
  }

  @Test
  def loadFromPackagist_givenInvalidPackage_expectError() = {
    val result = Packagist.loadVersions("some-unexisting-vendor/some-unexisting-package-123")

    assertTrue(result.isFailure)
  }
}
