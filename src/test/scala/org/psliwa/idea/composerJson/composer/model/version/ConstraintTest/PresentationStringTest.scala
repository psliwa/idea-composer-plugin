package org.psliwa.idea.composerJson.composer.model.version.ConstraintTest

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.composer.model.version._

class PresentationStringTest {

  val semVer120 = SemanticConstraint(new SemanticVersion(1, 2, 0))
  val semVer121 = SemanticConstraint(new SemanticVersion(1, 2, 1))

  @Test
  def testPresentationString(): Unit = {
    List(
      (semVer120, "1.2.0"),
      (DevConstraint("master"), "dev-master"),
      (WildcardConstraint(Some(semVer120)), "1.2.0.*"),
      (WildcardConstraint(None), "*"),
      (WrappedConstraint(semVer120, Some("prefix-"), Some("-suffix")), "prefix-1.2.0-suffix"),
      (OperatorConstraint(ConstraintOperator.<=, semVer120), "<=1.2.0"),
      (OperatorConstraint(ConstraintOperator.<=, semVer120, " "), "<= 1.2.0"),
      (HashConstraint("afafaf"), "afafaf"),
      (DateConstraint("20101010"), "20101010"),
      (HyphenRangeConstraint(semVer120, semVer120, " - "), "1.2.0 - 1.2.0"),
      (HyphenRangeConstraint(semVer120, semVer120, "-"), "1.2.0-1.2.0"),
      (AliasedConstraint(semVer120, semVer121, " as "), "1.2.0 as 1.2.1"),
      (AliasedConstraint(semVer120, semVer121, " AS "), "1.2.0 AS 1.2.1"),
      (LogicalConstraint(List(semVer120, semVer121), LogicalOperator.AND, ", "), "1.2.0, 1.2.1"),
      (LogicalConstraint(List(semVer120, semVer121), LogicalOperator.AND, ","), "1.2.0,1.2.1"),
      (LogicalConstraint(List(semVer120, semVer121), LogicalOperator.OR, " || "), "1.2.0 || 1.2.1"),
      (LogicalConstraint(List(semVer120, semVer121), LogicalOperator.OR, "|"), "1.2.0|1.2.1")
    ).foreach {
      case (constraint, expected) => assertEquals(expected, constraint.presentation)
    }
  }
}
