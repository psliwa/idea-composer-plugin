package org.psliwa.idea.composerJson.json

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.completion.ComposerSchemaFilepath

class SchemaLoadingTest {

  @Test
  def loadComposerCompletionSchema() = {
    assertNotEquals(None, SchemaLoader.load(ComposerSchemaFilepath))
  }

  @Test
  def loadComposerSchema_givenPathIsInvalid_returnNone() = {
    assertEquals(None, SchemaLoader.load("invalid-file"))
  }
}
