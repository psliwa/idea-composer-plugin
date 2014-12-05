package org.psliwa.idea.composer.test.parser

import org.junit.Test
import org.junit.Assert._
import org.psliwa.idea.composer.parser.SchemaLoader

class SchemaLoadingTest {

  @Test
  def loadComposerSchema() = {
    assertNotEquals(None, SchemaLoader.load())
  }

  @Test
  def loadComposerSchema_givenPathIsInvalid_returnNone() = {
    assertEquals(None, SchemaLoader.load("invalid-file"))
  }
}
