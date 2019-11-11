package org.psliwa.idea.composerJson.composer.model.version

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.composer.model.version.VersionSuggestions.SimplifiedVersionConstraint.{NonSemantic, PrefixedSemantic, PureSemantic, Wildcard}
import org.psliwa.idea.composerJson.composer.model.version.VersionSuggestions._

class VersionComparatorTest {

  @Test
  def sortVersions(): Unit = {
    val sortedVersions = sorted(List(
      PureSemantic(new SemanticVersion(1, 0, 0)),
      NonSemantic("dev-master"),
      PureSemantic(new SemanticVersion(1, 2)),
      PureSemantic(new SemanticVersion(1, 0, 100)),
      Wildcard(PureSemantic(new SemanticVersion(1, 0))),
      NonSemantic("1.1.x-dev"),
      PureSemantic(new SemanticVersion(1, 1)),
      Wildcard(PureSemantic(new SemanticVersion(1))),
      PureSemantic(new SemanticVersion(1, 1, 0)),
      PrefixedSemantic(PureSemantic(new SemanticVersion(1, 1, 0)))
    ))

    assertEquals(List("1.1.0", "1.0.*", "1.0.100", "1.0.0", "1.*", "1.2", "1.1", "v1.1.0", "dev-master", "1.1.x-dev"), sortedVersions)
  }

  @Test
  def sortSemanticVersionsNumerically(): Unit = {
    val versions = sorted(List("3.9.0", "3.33.0").flatMap(VersionSuggestions.parseSemantic))

    assertEquals(List("3.33.0", "3.9.0"), versions)
  }

  private def sorted(versions: List[SimplifiedVersionConstraint]): List[String] = {
    versions.sortWith(VersionSuggestions.isGreater).map(_.presentation)
  }
}
