package org.psliwa.idea.composerJson.composer.version

import org.junit.Assert._
import org.junit.Test

class ConstraintTest {

  val boundedConstraint = new SemanticConstraint(new SemanticVersion(1, 0))
  val unboundedConstraint = new WildcardConstraint(None)

  @Test
  def givenSemVer_itShouldBeBounded() = {
    checkBounded(SemanticConstraint(new SemanticVersion(1, 0, 0)))
  }

  @Test
  def givenWildcardedBoundedConstraint_itShouldBeBounded() = {
    checkBounded(new WildcardConstraint(Some(boundedConstraint)))
  }

  @Test
  def givenEmptyWildcard_itShouldBeUnbounded() = {
    checkUnbounded(new WildcardConstraint(None))
  }

  @Test
  def givenBoundedConstraintWithNsrOperator_itShouldBeBounded() = {
    List(ConstraintOperator.^, ConstraintOperator.~).foreach(operator => {
      checkBounded(new OperatorConstraint(operator, boundedConstraint))
    })
  }
  
  @Test
  def givenBoundedConstraintWithComparisonOperatorWithoutUpperBound_itShouldBeUnbounded() = {
    import ConstraintOperator._

    List(>=, >, ConstraintOperator.!=).foreach(operator => {
      checkUnbounded(new OperatorConstraint(operator, boundedConstraint))
    })
  }

  @Test
  def givenLogicalConstraintWithFewOperatorConstraintsWithoutUpperBound_itShouldBeUnbounded(): Unit = {
    import LogicalOperator._
    import ConstraintOperator._

    for {
      operators <- List(List(>=, >=), List(>=, >), List(ConstraintOperator.!=, ConstraintOperator.!=))
      logicalOp <- List(OR, AND)
    } yield checkUnbounded(LogicalConstraint(operators.map(OperatorConstraint(_, boundedConstraint)), logicalOp))
  }

  @Test
  def givenLogicalAndConstraintWithTwoUpperBoundedOperatorConstraint_itShouldBeBounded(): Unit = {
    import LogicalOperator._
    import ConstraintOperator._

    List(List(>, <), List(>=, <), List(>, <=), List(>=, <=), List(<=, <=), List(<, <=), List(<, ConstraintOperator.!=)).foreach(operators => {
      checkBounded(LogicalConstraint(operators.map(OperatorConstraint(_, boundedConstraint)), AND))
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
    import LogicalOperator._
    checkUnbounded(LogicalConstraint(List(unboundedConstraint, unboundedConstraint), AND))
  }

  @Test
  def givenLogicalAndConstraint_givenInternalConstraintsAreAllBounded_outerConstraintShouldBounded(): Unit = {
    import LogicalOperator._
    checkBounded(LogicalConstraint(List(boundedConstraint, boundedConstraint), AND))
  }

  @Test
  def givenLogicalAndConstraint_givenOneInternalConstraintIsBounded_outerConstraintShouldBounded(): Unit = {
    import LogicalOperator._
    checkBounded(LogicalConstraint(List(boundedConstraint, unboundedConstraint), AND))
  }

  @Test
  def givenLogicalOrConstraint_givenInternalConstraintAreAllUnbounded_outerConstraintShouldBeUnbounded(): Unit = {
    import LogicalOperator._
    checkUnbounded(LogicalConstraint(List(unboundedConstraint, unboundedConstraint), OR))
  }

  @Test
  def givenLogicalOrConstraint_givenInternalConstraintAreAllBounded_outerConstraintShouldBeBounded(): Unit = {
    import LogicalOperator._
    checkBounded(LogicalConstraint(List(boundedConstraint, boundedConstraint), OR))
  }

  @Test
  def givenLogicalOrConstraint_givenOneInternalConstraintIsUnbounded_outerConstraintShouldUnbounded(): Unit = {
    import LogicalOperator._
    checkUnbounded(LogicalConstraint(List(boundedConstraint, unboundedConstraint), OR))
  }

  @Test
  def givenSpecificConstraint_isShouldBeBounded(): Unit = {
    List(DateConstraint("20101010"), HashConstraint("abcde")).foreach(checkBounded(_))
  }

  @Test
  def semanticVersionAcceptsZeros(): Unit = {
    SemanticVersion(0, Some(0, Some(1, None)))
  }

  @Test(expected = classOf[IllegalArgumentException])
  def semanticVersionsDoesNotAcceptNegativeInts(): Unit = {
    SemanticVersion(-1, Some(1, None))
  }

  private def checkBounded(constraint: Constraint, expected: Boolean = true): Unit = {
    assertEquals(constraint.toString, expected, constraint.isBounded)
  }

  private def checkUnbounded(constraint: Constraint): Unit = {
    checkBounded(constraint, expected = false)
  }
}