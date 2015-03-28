package org.psliwa.idea.composerJson.composer.version

import org.junit.Test
import org.junit.Assert._

class SemanticVersionTest {
  @Test
  def semanticVersionAcceptsZeros(): Unit = {
    SemanticVersion(0, Some(0, Some(1)))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def semanticVersionsDoesNotAcceptNegativeInts(): Unit = {
    SemanticVersion(-1, Some(1, None))
  }

  @Test
  def incrementLastPart(): Unit = {
    List(
      (new SemanticVersion(1, 2, 3), new SemanticVersion(1, 2, 4)),
      (new SemanticVersion(1, 2), new SemanticVersion(1, 3)),
      (new SemanticVersion(1), new SemanticVersion(2))
    ).foreach{
      case (actual, expected) => assertEquals(expected, actual.incrementLast)
    }
  }

  @Test
  def dropLastPart(): Unit = {
    List(
      (new SemanticVersion(1, 2, 3), new SemanticVersion(1, 2)),
      (new SemanticVersion(1, 2), new SemanticVersion(1)),
      (new SemanticVersion(1), null)
    ).foreach{
      case (actual, expected) => assertEquals(Option(expected), actual.dropLast)
    }
  }

  @Test
  def appendPart(): Unit = {
    List(
      (new SemanticVersion(1, 2, 3), null),
      (new SemanticVersion(1, 2), new SemanticVersion(1, 2, 0)),
      (new SemanticVersion(1), new SemanticVersion(1, 0))
    ).foreach{
      case (actual, expected) => assertEquals(Option(expected), actual.append(0))
    }
  }

  @Test
  def fillZero(): Unit = {
    List(
      (new SemanticVersion(1, 2, 3), new SemanticVersion(1, 2, 3)),
      (new SemanticVersion(1, 2), new SemanticVersion(1, 2, 0)),
      (new SemanticVersion(1), new SemanticVersion(1, 0, 0))
    ).foreach{
      case (actual, expected) => assertEquals(expected, actual.fillZero)
    }
  }

  @Test
  def ensuringParts(): Unit = {
    List(
      (new SemanticVersion(1, 2, 3), new SemanticVersion(1, 2, 3)),
      (new SemanticVersion(1, 2), new SemanticVersion(1, 2)),
      (new SemanticVersion(1), new SemanticVersion(1, 0))
    ).foreach{
      case (actual, expected) => assertEquals(expected, actual.ensureParts(2))
    }
  }

  @Test
  def ensuringExactlyParts(): Unit = {
    List(
      (new SemanticVersion(1, 2, 3), new SemanticVersion(1, 2)),
      (new SemanticVersion(1, 2), new SemanticVersion(1, 2)),
      (new SemanticVersion(1), new SemanticVersion(1, 0))
    ).foreach{
      case (actual, expected) => assertEquals(expected, actual.ensureExactlyParts(2))
    }
  }

  @Test
  def droppingZeros(): Unit = {
    List(
      (new SemanticVersion(1, 2, 3), new SemanticVersion(1, 2, 3)),
      (new SemanticVersion(1, 2, 0), new SemanticVersion(1, 2)),
      (new SemanticVersion(1, 0, 0), new SemanticVersion(1)),
      (new SemanticVersion(1, 0, 1), new SemanticVersion(1, 0, 1))
    ).foreach{
      case (actual, expected) => assertEquals(expected, actual.dropZeros)
    }
  }
}
