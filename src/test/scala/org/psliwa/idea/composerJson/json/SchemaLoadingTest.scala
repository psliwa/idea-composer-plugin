package org.psliwa.idea.composerJson.json

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.ComposerSchemaFilepath

class SchemaLoadingTest {

  @Test
  def loadComposerCompletionSchema(): Unit = {
    assertNotEquals(None, SchemaLoader.load(ComposerSchemaFilepath))
  }

  @Test
  def loadComposerSchema_givenPathIsInvalid_returnNone(): Unit = {
    assertEquals(None, SchemaLoader.load("invalid-file"))
  }
}
