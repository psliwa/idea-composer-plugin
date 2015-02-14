package org.psliwa.idea.composerJson.composer.version

import org.junit.Assert._
import org.junit.Test

class VersionComparatorTest {

  @Test
  def sortVersions() = {
    val versions = List("1.0.0", "dev-master", "1.2", "1.0.100", "1.0.*", "1.1.x-dev", "1.1", "1.*", "1.1.0", "v1.1.0").sortWith(Version.isGreater)

    assertEquals(List("1.1.0", "1.0.*", "1.0.100", "1.0.0", "1.*", "1.2", "1.1", "v1.1.0", "dev-master", "1.1.x-dev"), versions)
  }
}
