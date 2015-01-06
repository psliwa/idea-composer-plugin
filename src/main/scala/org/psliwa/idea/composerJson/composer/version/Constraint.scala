package org.psliwa.idea.composerJson.composer.version

sealed trait Constraint {
  def isBounded: Boolean = this match {
    case SemanticConstraint(_) => true
    case WildcardConstraint(None) => false
    case WildcardConstraint(Some(constraint)) => constraint.isBounded
    case WrappedConstraint(constraint, _, _) => constraint.isBounded
    case OperatorConstraint(operator, constraint) => operator.isBounded && constraint.isBounded
    case LogicalConstraint(constraints, operator) => operator match {
      case LogicalOperator.AND => constraints.exists(_.isBounded)
      case LogicalOperator.OR => constraints.forall(_.isBounded)
    }
    case AliasedConstraint(constraint, _) => constraint.isBounded
    case HashConstraint(_) | HyphenRangeConstraint(_, _) | DateConstraint(_) => true
    case DevConstraint(_) => false
    case _ => false
  }

  def replace(f: Constraint => Option[Constraint]): Constraint = {
    f(this).getOrElse( this match {
      case WrappedConstraint(constraint, prefix, suffix) => WrappedConstraint(constraint.replace(f), prefix, suffix)
      case WildcardConstraint(Some(constraint)) => constraint.replace(f) match {
        case sc@SemanticConstraint(_) => WildcardConstraint(Some(sc))
        case _ => this
      }
      case OperatorConstraint(operator, constraint) => OperatorConstraint(operator, constraint.replace(f))
      case AliasedConstraint(constraint, alias) => AliasedConstraint(constraint.replace(f), alias)
      case HyphenRangeConstraint(from, to) => HyphenRangeConstraint(from.replace(f), to.replace(f))
      case LogicalConstraint(constraints, operator) => LogicalConstraint(constraints.map(_.replace(f)), operator)
      case _ => this
    })
  }
}

case class SemanticConstraint(version: SemanticVersion) extends Constraint
case class WildcardConstraint(constraint: Option[SemanticConstraint]) extends Constraint
case class WrappedConstraint(constraint: Constraint, prefix: Option[String], suffix: Option[String]) extends Constraint
case class OperatorConstraint(operator: ConstraintOperator, constraint: Constraint) extends Constraint
case class LogicalConstraint(constraints: List[Constraint], operator: LogicalOperator) extends Constraint
case class AliasedConstraint(constraint: Constraint, as: Constraint) extends Constraint
case class HashConstraint(version: String) extends Constraint
case class DateConstraint(version: String) extends Constraint
case class DevConstraint(version: String) extends Constraint
case class HyphenRangeConstraint(from: Constraint, to: Constraint) extends Constraint

sealed trait ConstraintOperator {
  def isBounded = true
}
sealed trait UnboundedOperator extends ConstraintOperator {
  override def isBounded = false
}

object ConstraintOperator {
  case object >= extends UnboundedOperator
  case object > extends UnboundedOperator
  case object < extends ConstraintOperator
  case object <= extends ConstraintOperator
  case object != extends UnboundedOperator
  case object ~ extends ConstraintOperator
  case object ^ extends ConstraintOperator

  val values = Set(>=, >, <, <=, !=, ConstraintOperator.~, ^)
}

sealed trait LogicalOperator

object LogicalOperator {
  case object OR extends LogicalOperator
  case object AND extends LogicalOperator
}

