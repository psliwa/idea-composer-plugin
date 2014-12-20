package org.psliwa.idea.composer.version

import org.junit.Test
import org.junit.Assert._

class VersionTest {
  @Test
  def givenNonSemanticVersion_versionShouldNotHasAnAlternatives() = {
    val version = "dev-master"

    assertEquals(List(version), Version.alternativesForPrefix("")(version))
  }

  @Test
  def givenSemanticVersion_wildcardShouldBeInAlternatives() = {
    val version = "1.2.3"

    val expected = List(version, "1.2.*", "1.*")
    val unexpected = List()

    assertVersionAlternatives(expected, unexpected, Version.alternativesForPrefix("")(version))
  }

  @Test
  def givenNotNormalizedSemanticVersion_wildcardShouldBeInAlternatives() = {
    val version = "v1.2.3"

    val expected = List(version, "1.2.3", "1.2.*", "1.*")
    val unexpected = List()

    assertVersionAlternatives(expected, unexpected, Version.alternativesForPrefix("")(version))
  }

  @Test
  def givenSemanticVersion_normalizedVersionShouldBeOnlyOne() = {
    val version = "1.2.3"

    assertEquals(1, Version.alternativesForPrefix("")(version).count(_ == version))
  }

  @Test
  def givenSemanticVersion_givenTildePrefix_twoDigitsVersionsShouldBeAnAlternative() = {
    val version = "1.2.3"

    assertVersionAlternatives(List("1.2"), List(version, "1.2.*"), Version.alternativesForPrefix("~")(version))
  }

  @Test
  def givenSemanticVersion_givenPeakPrefix_threeAndTwoDigitsVersionsShouldBeAnAlternative() = {
    val version = "1.2.3"

    assertVersionAlternatives(List("1.2.3", "1.2"), List("1.2.*"), Version.alternativesForPrefix("^")(version))
  }

  @Test
  def givenSemanticVersion_givenPeakWithVersionAndWhitespace_allAlternativesAreExpected() = {
    val version = "1.2.3"

    assertVersionAlternatives(List("1.2.3", "1.2", "1.2.*"), List(), Version.alternativesForPrefix("^3.2.1 <caret>")(version))
  }

  private def assertVersionAlternatives(expected: List[String], actualAlternatives: List[String]) {
    assertVersionAlternatives(expected, List(), actualAlternatives)
  }

  private def assertVersionAlternatives(expected: List[String], unexpected: List[String], actualAlternatives: List[String]) {
    expected.foreach(
      expectedVersion => assertTrue(expectedVersion + " should be in "+actualAlternatives, actualAlternatives.contains(expectedVersion))
    )

    unexpected.foreach(
      version => assertFalse(version + " should not be in "+actualAlternatives, actualAlternatives.contains(version))
    )
  }
}
