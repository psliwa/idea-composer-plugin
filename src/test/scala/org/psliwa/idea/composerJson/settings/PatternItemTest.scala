package org.psliwa.idea.composerJson.settings

import org.junit.Assert._
import org.junit.Test

class PatternItemTest {

  @Test
  def givenExactPattern_givenTheSameText_itShouldMatch() = {
    assertPatternMatch("vendor/pkg", "vendor/pkg")
  }

  @Test
  def givenExactPattern_givenDifferentText_itShouldNotMatch() = {
    assertPatternNotMatch("vendor/pkg", "vendor2/pkg")
  }

  @Test
  def givenExactPattern_givenText_patternIsTextPrefix_itShouldNotMatch() = {
    assertPatternNotMatch("vendor/pkg", "vendor/pkg2")
  }

  @Test
  def givenWildcardPattern_givenMatchingText_itShouldMatch() = {
    assertPatternMatch("vendor/*", "vendor/pkg")
  }

  @Test
  def givenWildcardPattern_givenWildcardIsInTheMiddle_givenMatchingText_itShouldMatch() = {
    assertPatternMatch("vend*kg", "vendor/pkg")
  }

  @Test
  def givenInvalidPattern_isShouldNotMatch() = {
    assertPatternNotMatch("[ ? abc .-", "vendor/pkg")
  }

  @Test
  def givenEmptyPattern_isShouldNotMatch() = {
    assertPatternNotMatch("", "vendor/pkg")
  }

  private def assertPatternMatch(pattern: String, text: String, expectedMatch: Boolean = true) = {
    assertEquals(expectedMatch, new PatternItem(pattern).matches(text))
  }

  private def assertPatternNotMatch(pattern: String, text: String) = {
    assertPatternMatch(pattern, text, expectedMatch = false)
  }
}
