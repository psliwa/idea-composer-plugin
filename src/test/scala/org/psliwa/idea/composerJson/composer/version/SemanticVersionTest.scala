package org.psliwa.idea.composerJson.composer.version

import org.junit.Test
import org.junit.Assert._

class SemanticVersionTest {
  @Test
  def semanticVersionAcceptsZeros(): Unit = {
    SemanticVersion(0, Some(0, Some(1, None)))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def semanticVersionsDoesNotAcceptNegativeInts(): Unit = {
    SemanticVersion(-1, Some(1, None))
  }

  @Test
  def incrementLastPart(): Unit = {
    List(
      (new SemanticVersion(1, 2, 3, 4), new SemanticVersion(1, 2, 3, 5)),
      (new SemanticVersion(1, 2, 3), new SemanticVersion(1, 2, 4)),
      (new SemanticVersion(1, 2), new SemanticVersion(1, 3)),
      (new SemanticVersion(1), new SemanticVersion(2))
    ).foreach{
      case (actual, expected) => assertEquals(expected, actual.incrementLast)
    }
  }
}
