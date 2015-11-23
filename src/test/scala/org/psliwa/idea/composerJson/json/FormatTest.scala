package org.psliwa.idea.composerJson.json

import org.junit.Assert._
import org.junit.Test

class FormatTest {
  @Test
  def givenValidUrl_itShouldBeValidUri() = {
    assertTrue(UriFormat.isValid("http://somevalid.url.com/some?query=123"))
  }

  @Test
  def givenInvalidUrl_itShouldBeInvalidUri() = {
    assertFalse(UriFormat.isValid("invalid url"))
  }

  @Test
  def givenEmails_checkTheyValidity() = {
    val emails = List(
      "me@psliwa.org" -> true,
      "some+123@gmail.com" -> true,
      "some.value@xyz.xyz.com" -> true,
      "some value@xyz.xyz.com" -> false,
      "some.value@xyz" -> false,
      "abc" -> false
    )

    assertEquals(emails.map(_._2), emails.map { case (email, _) => EmailFormat.isValid(email) })
  }
}
