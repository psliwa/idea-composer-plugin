package org.psliwa.idea.composerJson.intellij.codeAssist.schema

import org.junit.Test
import org.junit.Assert._
import org.psliwa.idea.composerJson.intellij.CharContainsMatcher

class CharContainsMatcherTest {

  @Test
  def givenExactPrefix_expectMatch() = {
    assertTrue(matches("some text", "some text"))
  }

  @Test
  def givenPartialPrefix_expectMatch() = {
    assertTrue(matches("some", "some text"))
  }

  @Test
  def givenEverySecondCharPrefix_expectMatch() = {
    assertTrue(matches("smtx", "some text"))
  }

  @Test
  def givenEveryCharsButInDifferentOrderAsPrefix_expectNotMatch() = {
    assertFalse(matches("emos txet", "some text"))
  }

  @Test
  def givenThreeTimesTheSameChar_inGivenValueTheCharOccursOnlyTwice_expectNotMatch() = {
    assertFalse(matches("222", "2.2.8"))
  }

  @Test
  def givenPrefixIsLongerThanValue_expectNotMatch() = {
    assertFalse(matches("some text and more", "some text"))
  }

  @Test
  def givenAnagram_expectNotMatch() = {
    assertFalse(matches("smeo", "some"))
  }

  @Test
  def givenAnagramPlusExtraChars_expectNotMatch() = {
    assertFalse(matches("smeoe", "some"))
  }

  private def matches(prefix: String, value: String): Boolean = {
    new CharContainsMatcher(prefix).prefixMatches(value)
  }
}
