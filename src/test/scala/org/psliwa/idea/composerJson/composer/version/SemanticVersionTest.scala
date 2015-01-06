package org.psliwa.idea.composerJson.composer.version

import org.junit.Test

class SemanticVersionTest {
  @Test
  def semanticVersionAcceptsZeros(): Unit = {
    SemanticVersion(0, Some(0, Some(1, None)))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def semanticVersionsDoesNotAcceptNegativeInts(): Unit = {
    SemanticVersion(-1, Some(1, None))
  }
}
