package org.psliwa.idea.composerJson.composer.version

import org.junit.Assert._
import org.junit.Test

class VersionAlternativesTest {
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
  def givenSemanticVersion_givenNsrPrefix_semanticVersionsShouldBeAnAlternative() = {
    val version = "1.2.3"

    List("~", "^").foreach(operator => {
      assertVersionAlternatives(List("1.2", "1.2.3"), List("1.2.*"), Version.alternativesForPrefix(operator)(version))
    })
  }

  @Test
  def givenSemanticVersionWithVPrevix_givenNsrPrefix_semanticVersionsShouldBeAnAlternative() = {
    val version = "v1.2.3"

    List("~", "^").foreach(operator => {
      assertVersionAlternatives(List("1.2", "1.2.3"), List("1.2.*", "v1.2.3"), Version.alternativesForPrefix(operator)(version))
    })
  }

  @Test
  def givenSemanticVersion_givenCaretWithVersionAndSpace_allAlternativesAreExpected() = {
    val version = "1.2.3"

    assertVersionAlternatives(List("1.2.3", "1.2.*"), List(), Version.alternativesForPrefix("^3.2.1 ")(version))
  }

  @Test
  def givenSemanticVersion_givenComparisonPrefix_threeAndTwoDigitsVersionsShouldBeAnAlternative() = {
    val version = "1.2.3"

    for(prefix <- List(">=", ">", "<", "<=")) {
      assertVersionAlternatives(List("1.2.3", "1.2"), List("1.2.*"), Version.alternativesForPrefix(prefix)(version))
    }
  }

  @Test
  def givenSemanticVersion_givenSpaceWithComparisonPrefix_threeAndTwoDigitsVersionsShouldBeAnAlternative() = {
    val version = "1.2.3"

    for(prefix <- List(">=", ">", "<", "<=")) {
      assertVersionAlternatives(List("1.2.3", "1.2"), List("1.2.*"), Version.alternativesForPrefix("1.2.1 "+prefix)(version))
    }
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
