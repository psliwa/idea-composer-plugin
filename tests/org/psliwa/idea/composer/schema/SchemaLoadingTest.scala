package org.psliwa.idea.composer.schema

import org.junit.Test
import org.junit.Assert._
import org.psliwa.idea.composer.schema.SchemaLoader

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
