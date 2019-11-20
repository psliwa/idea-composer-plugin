package org.psliwa.idea.composerJson.composer.model.version.ConstraintTest

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.composer.model.version._

class UpperBoundTest {

  val boundedConstraint = SemanticConstraint(new SemanticVersion(1, 0))
  val unboundedConstraint = WildcardConstraint(None)

  @Test
  def givenSemVer_itShouldBeBounded(): Unit = {
    checkBounded(SemanticConstraint(new SemanticVersion(1, 0, 0)))
  }

  @Test
  def givenWildcardedBoundedConstraint_itShouldBeBounded(): Unit = {
    checkBounded(WildcardConstraint(Some(boundedConstraint)))
  }

  @Test
  def givenEmptyWildcard_itShouldBeUnbounded(): Unit = {
    checkUnbounded(WildcardConstraint(None))
  }

  @Test
  def givenBoundedConstraintWithNsrOperator_itShouldBeBounded(): Unit = {
    List(ConstraintOperator.^, ConstraintOperator.~).foreach(operator => {
      checkBounded(OperatorConstraint(operator, boundedConstraint))
    })
  }

  @Test
  def givenBoundedConstraintWithComparisonOperatorWithoutUpperBound_itShouldBeUnbounded(): Unit = {
    import org.psliwa.idea.composerJson.composer.model.version.ConstraintOperator._

    List(>=, >, ConstraintOperator.!=).foreach(operator => {
      checkUnbounded(new OperatorConstraint(operator, boundedConstraint))
    })
  }

  @Test
  def givenLogicalConstraintWithFewOperatorConstraintsWithoutUpperBound_itShouldBeUnbounded(): Unit = {
    import org.psliwa.idea.composerJson.composer.model.version.ConstraintOperator._
    import org.psliwa.idea.composerJson.composer.model.version.LogicalOperator._

    for {
      operators <- List(List(>=, >=), List(>=, >), List(ConstraintOperator.!=, ConstraintOperator.!=))
      logicalOp <- List(OR, AND)
    } yield checkUnbounded(LogicalConstraint(operators.map(OperatorConstraint(_, boundedConstraint)), logicalOp, ""))
  }

  @Test
  def givenLogicalAndConstraintWithTwoUpperBoundedOperatorConstraint_itShouldBeBounded(): Unit = {
    import org.psliwa.idea.composerJson.composer.model.version.ConstraintOperator._
    import org.psliwa.idea.composerJson.composer.model.version.LogicalOperator._

    List(List(>, <), List(>=, <), List(>, <=), List(>=, <=), List(<=, <=), List(<, <=), List(<, ConstraintOperator.!=))
      .foreach(operators => {
        checkBounded(LogicalConstraint(operators.map(OperatorConstraint(_, boundedConstraint)), AND, " "))
      })
  }

  @Test
  def givenWrappedBoundedConstraint_itShouldBeBounded(): Unit = {
    checkBounded(WrappedConstraint(boundedConstraint, None, None))
  }

  @Test
  def givenWrappedUnboundedConstraint_itShouldBeUnbounded(): Unit = {
    checkUnbounded(WrappedConstraint(unboundedConstraint, None, None))
  }

  @Test
  def givenAliasedBoundedConstraint_itShouldBeBounded(): Unit = {
    checkBounded(AliasedConstraint(boundedConstraint, unboundedConstraint))
  }

  @Test
  def givenAliasedUnboundedConstraint_itShouldBeUnbounded(): Unit = {
    checkUnbounded(AliasedConstraint(unboundedConstraint, unboundedConstraint))
  }

  @Test
  def givenLogicalAndConstraint_givenInternalConstraintsAreAllUnbounded_outerConstraintShouldBeUnbounded(): Unit = {
    import org.psliwa.idea.composerJson.composer.model.version.LogicalOperator._
    checkUnbounded(LogicalConstraint(List(unboundedConstraint, unboundedConstraint), AND, " "))
  }

  @Test
  def givenLogicalAndConstraint_givenInternalConstraintsAreAllBounded_outerConstraintShouldBounded(): Unit = {
    import org.psliwa.idea.composerJson.composer.model.version.LogicalOperator._
    checkBounded(LogicalConstraint(List(boundedConstraint, boundedConstraint), AND, " "))
  }

  @Test
  def givenLogicalAndConstraint_givenOneInternalConstraintIsBounded_outerConstraintShouldBounded(): Unit = {
    import org.psliwa.idea.composerJson.composer.model.version.LogicalOperator._
    checkBounded(LogicalConstraint(List(boundedConstraint, unboundedConstraint), AND, " "))
  }

  @Test
  def givenLogicalOrConstraint_givenInternalConstraintAreAllUnbounded_outerConstraintShouldBeUnbounded(): Unit = {
    import org.psliwa.idea.composerJson.composer.model.version.LogicalOperator._
    checkUnbounded(LogicalConstraint(List(unboundedConstraint, unboundedConstraint), OR, "|"))
  }

  @Test
  def givenLogicalOrConstraint_givenInternalConstraintAreAllBounded_outerConstraintShouldBeBounded(): Unit = {
    import org.psliwa.idea.composerJson.composer.model.version.LogicalOperator._
    checkBounded(LogicalConstraint(List(boundedConstraint, boundedConstraint), OR, "|"))
  }

  @Test
  def givenLogicalOrConstraint_givenOneInternalConstraintIsUnbounded_outerConstraintShouldUnbounded(): Unit = {
    import org.psliwa.idea.composerJson.composer.model.version.LogicalOperator._
    checkUnbounded(LogicalConstraint(List(boundedConstraint, unboundedConstraint), OR, "|"))
  }

  @Test
  def givenSpecificConstraint_isShouldBeBounded(): Unit = {
    List(DateConstraint("20101010"), HashConstraint("abcde")).foreach(checkBounded(_))
  }

  private def checkBounded(constraint: Constraint, expected: Boolean = true): Unit = {
    assertEquals(constraint.toString, expected, constraint.isBounded)
  }

  private def checkUnbounded(constraint: Constraint): Unit = {
    checkBounded(constraint, expected = false)
  }
}
