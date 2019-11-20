package org.psliwa.idea.composerJson.composer.model.version

sealed trait Constraint {
  def isBounded: Boolean = this match {
    case SemanticConstraint(_) => true
    case WildcardConstraint(None) => false
    case WildcardConstraint(Some(constraint)) => constraint.isBounded
    case WrappedConstraint(constraint, _, _) => constraint.isBounded
    case OperatorConstraint(operator, constraint, _) => operator.isBounded && constraint.isBounded
    case LogicalConstraint(constraints, operator, _) =>
      operator match {
        case LogicalOperator.AND => constraints.exists(_.isBounded)
        case LogicalOperator.OR => constraints.forall(_.isBounded)
      }
    case AliasedConstraint(constraint, _, _) => constraint.isBounded
    case HashConstraint(_) | HyphenRangeConstraint(_, _, _) | DateConstraint(_) => true
    case DevConstraint(_) => false
    case _ => false
  }

  def replace(f: Constraint => Option[Constraint]): Constraint = {
    f(this).getOrElse(this match {
      case WrappedConstraint(constraint, prefix, suffix) => WrappedConstraint(constraint.replace(f), prefix, suffix)
      case WildcardConstraint(Some(constraint)) =>
        constraint.replace(f) match {
          case sc @ SemanticConstraint(_) => WildcardConstraint(Some(sc))
          case _ => this
        }
      case OperatorConstraint(operator, constraint, ps) => OperatorConstraint(operator, constraint.replace(f), ps)
      case AliasedConstraint(constraint, alias, ps) => AliasedConstraint(constraint.replace(f), alias, ps)
      case HyphenRangeConstraint(from, to, ps) => HyphenRangeConstraint(from.replace(f), to.replace(f), ps)
      case LogicalConstraint(constraints, operator, ps) =>
        LogicalConstraint(constraints.map(_.replace(f)), operator, ps)
      case _ => this
    })
  }

  def contains(f: Constraint => Boolean): Boolean = {
    if (f(this)) true
    else
      this match {
        case WrappedConstraint(constraint, _, _) => constraint.contains(f)
        case WildcardConstraint(Some(constraint)) => constraint.contains(f)
        case OperatorConstraint(_, constraint, _) => constraint.contains(f)
        case AliasedConstraint(constraint, alias, _) => constraint.contains(f) || alias.contains(f)
        case HyphenRangeConstraint(from, to, _) => from.contains(f) || to.contains(f)
        case LogicalConstraint(constraints, _, _) => constraints.exists(f)
        case _ => false
      }
  }

  def presentation: String = this match {
    case SemanticConstraint(version) => version.toString
    case DevConstraint(version) => "dev-" + version
    case WildcardConstraint(maybeConstraint) => maybeConstraint.map(_.presentation + ".").getOrElse("") + "*"
    case WrappedConstraint(constraint, prefix, suffix) =>
      prefix.map(_.toString).getOrElse("") + constraint.presentation + suffix.map(_.toString).getOrElse("")
    case OperatorConstraint(operator, constraint, separator) => operator.toString + separator + constraint.presentation
    case DateConstraint(version) => version
    case HashConstraint(version) => version
    case HyphenRangeConstraint(from, to, separator) => from.presentation + separator + to.presentation
    case AliasedConstraint(constraint, alias, separator) => constraint.presentation + separator + alias.presentation
    case LogicalConstraint(constraints, LogicalOperator.AND, separator) =>
      constraints.map(_.presentation).mkString(separator)
    case LogicalConstraint(constraints, LogicalOperator.OR, separator) =>
      constraints.map(_.presentation).mkString(separator)
    case _ => "<unknown>"
  }
}

case class SemanticConstraint(version: SemanticVersion) extends Constraint
case class WildcardConstraint(constraint: Option[SemanticConstraint]) extends Constraint
case class WrappedConstraint(constraint: Constraint, prefix: Option[String], suffix: Option[String]) extends Constraint
case class OperatorConstraint(operator: ConstraintOperator, constraint: Constraint, presentationPadding: String = "")
    extends Constraint
case class LogicalConstraint(constraints: List[Constraint], operator: LogicalOperator, presentationSeparator: String)
    extends Constraint
case class AliasedConstraint(constraint: Constraint, as: Constraint, presentationSeparator: String = " as ")
    extends Constraint
case class HashConstraint(version: String) extends Constraint
case class DateConstraint(version: String) extends Constraint
case class DevConstraint(version: String) extends Constraint
case class HyphenRangeConstraint(from: Constraint, to: Constraint, presentationSeparator: String = " - ")
    extends Constraint

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

  val values: Set[ConstraintOperator] = Set(>=, >, <, <=, !=, ConstraintOperator.~, ^)
}

sealed trait LogicalOperator

object LogicalOperator {
  case object OR extends LogicalOperator
  case object AND extends LogicalOperator
}
