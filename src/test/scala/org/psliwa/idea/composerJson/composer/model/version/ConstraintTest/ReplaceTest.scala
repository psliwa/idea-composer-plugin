package org.psliwa.idea.composerJson.composer.model.version.ConstraintTest

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.composer.model.version._

class ReplaceTest {

  val semVer120 = SemanticConstraint(new SemanticVersion(1, 2, 0))
  val semVer121 = SemanticConstraint(new SemanticVersion(1, 2, 1))

  @Test
  def givenPrimitiveConstraint(): Unit = {
    List(DevConstraint("master"), semVer120, DateConstraint("abc"), HashConstraint("afafaf")).foreach(constraint => {
      assertEquals(
        DevConstraint("trunk"),
        constraint.replace(_ => Some(DevConstraint("trunk")))
      )
    })
  }

  @Test
  def givenFuncProducesNone_constraintShouldBeUnchanged(): Unit = {
    assertEquals(
      DevConstraint("master"),
      DevConstraint("master").replace(_ => None)
    )
  }

  @Test
  def givenWrappedConstraint_givenFuncChangesInnerConstraint(): Unit = {
    assertEquals(
      WrappedConstraint(DevConstraint("trunk"), None, None),
      WrappedConstraint(DevConstraint("master"), None, None).replace {
        case DevConstraint(_) => Some(DevConstraint("trunk"))
        case _ => None
      }
    )
  }

  @Test
  def givenWrappedConstraint_givenFuncChangesOuterConstraint(): Unit = {
    assertEquals(
      DevConstraint("trunk"),
      WrappedConstraint(DevConstraint("master"), None, None).replace {
        case WrappedConstraint(_, _, _) => Some(DevConstraint("trunk"))
        case _ => None
      }
    )
  }

  @Test
  def givenWildcardConstraint_givenFuncChangesInnerConstraint(): Unit = {
    assertEquals(
      WildcardConstraint(Some(semVer121)),
      WildcardConstraint(Some(semVer120)).replace {
        case SemanticConstraint(_) => Some(semVer121)
        case _ => None
      }
    )
  }

  @Test
  def givenWildcardConstraint_givenFuncChangesInnerConstraint_changedConstraintIsNotSupportedByWildcard_originalConstraintShouldBeReturned()
      : Unit = {
    assertEquals(
      WildcardConstraint(Some(semVer120)),
      WildcardConstraint(Some(semVer120)).replace {
        case SemanticConstraint(_) => Some(DevConstraint("master"))
        case _ => None
      }
    )
  }

  @Test
  def givenOperatorConstraint_givenFuncChangesInnerConstraint(): Unit = {
    assertEquals(
      OperatorConstraint(ConstraintOperator.>, semVer121),
      OperatorConstraint(ConstraintOperator.>, semVer120).replace {
        case SemanticConstraint(_) => Some(semVer121)
        case _ => None
      }
    )
  }

  @Test
  def givenAliasedConstraint_givenFuncChangesInnerConstraint(): Unit = {
    assertEquals(
      AliasedConstraint(semVer121, semVer121),
      AliasedConstraint(semVer120, semVer121).replace {
        case SemanticConstraint(_) => Some(semVer121)
        case _ => None
      }
    )
  }

  @Test
  def givenHyphenRangeConstraint_givenFuncChangesInnerConstraints(): Unit = {
    assertEquals(
      HyphenRangeConstraint(semVer121, semVer121),
      HyphenRangeConstraint(semVer120, semVer121).replace {
        case SemanticConstraint(_) => Some(semVer121)
        case _ => None
      }
    )
  }

  @Test
  def givenLogicalConstraint_givenFuncChangesInnerConstraints(): Unit = {
    List(LogicalOperator.AND, LogicalOperator.OR).foreach(operator => {
      assertEquals(
        LogicalConstraint(List(semVer121, semVer121), operator, " "),
        LogicalConstraint(List(semVer120, semVer121), operator, " ").replace {
          case SemanticConstraint(_) => Some(semVer121)
          case _ => None
        }
      )
    })
  }
}
