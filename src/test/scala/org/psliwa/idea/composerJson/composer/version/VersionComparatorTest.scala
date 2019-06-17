package org.psliwa.idea.composerJson.composer.version

import org.junit.Assert._
import org.junit.Test

import scala.annotation.tailrec

class VersionComparatorTest {

  @Test
  def sortVersions() = {
    val sortedVersions = List("1.0.0", "dev-master", "1.2", "1.0.100", "1.0.*", "1.1.x-dev", "1.1", "1.*", "1.1.0", "v1.1.0").sortWith(Version.isGreater)

    assertDescendingVersionsOrder(sortedVersions)

    assertEquals(List("1.1.0", "1.0.*", "1.0.100", "1.0.0", "1.*", "1.2", "1.1", "v1.1.0", "dev-master", "1.1.x-dev"), sortedVersions)
  }

  @tailrec
  private def assertDescendingVersionsOrder(versions: List[String]): Unit = {
    versions.headOption match {
      case Some(version) =>
        val remainingVersions = versions.tail
        remainingVersions.foreach { version2 =>
          assertTrue(Version.isGreater(version, version2))
        }
        assertDescendingVersionsOrder(remainingVersions)
      case None =>
    }
  }

  @Test
  def sortSemanticVersionsNumerically() = {
    val versions = List("3.9.0", "3.33.0").sortWith(Version.isGreater)

    assertEquals(List("3.33.0", "3.9.0"), versions)
  }
}
