package org.psliwa.idea.composerJson.composer.model.version

import scala.language.postfixOps

object VersionEquivalents {
  def equivalentsFor(version: Constraint): Seq[Constraint] = {
    nsrEquivalent(version).toList
  }

  private def nsrEquivalent(version: Constraint): Option[Constraint] = {
    def incrementVersion(version: SemanticVersion): Option[SemanticVersion] = {
      version.dropLast
        .map(_.incrementLast)
        .flatMap(_.append(0))
    }

    Option(version.replace {
      //~ support
      case OperatorConstraint(ConstraintOperator.~, SemanticConstraint(versionFrom), _) =>
        incrementVersion(versionFrom.ensureParts(2))
          .map(versionTo => versionRange(versionFrom.ensureParts(2), versionTo.fillZero))
      //example: >=1.2 <3.0.0 to ~1.2
      case VersionRange(versionFrom, versionTo)
          if versionFrom.dropZeros.partsNumber < 3 && incrementVersion(versionFrom.ensureExactlyParts(2))
            .exists(_.fillZero == versionTo.fillZero) =>
        Some(OperatorConstraint(ConstraintOperator.~, SemanticConstraint(versionFrom.dropZeros.ensureParts(2)), ""))
      //example: >=1.2.1 <1.3.0 to ~1.2.1
      case VersionRange(versionFrom, versionTo)
          if incrementVersion(versionFrom.fillZero).exists(_.fillZero == versionTo.fillZero) =>
        Some(OperatorConstraint(ConstraintOperator.~, SemanticConstraint(versionFrom.ensureParts(3)), ""))
      //^ support for pre-release: ^0.3.1 to >=0.3.1 <0.4.0
      case OperatorConstraint(ConstraintOperator.^, SemanticConstraint(versionFrom), _)
          if versionFrom.partsNumber > 1 && versionFrom.major == 0 =>
        incrementVersion(versionFrom.ensureParts(3))
          .map(versionTo => versionRange(versionFrom, versionTo.fillZero))
      //^ support
      case OperatorConstraint(ConstraintOperator.^, SemanticConstraint(versionFrom), _) =>
        incrementVersion(versionFrom.ensureExactlyParts(2))
          .map(versionTo => versionRange(versionFrom, versionTo.fillZero))
      //example: >=1.2.1 <2.0.0 to ^1.2.1
      case VersionRange(versionFrom, versionTo)
          if versionFrom.partsNumber == 3 && incrementVersion(versionFrom.ensureExactlyParts(2))
            .exists(_.fillZero == versionTo.fillZero) =>
        Some(OperatorConstraint(ConstraintOperator.^, SemanticConstraint(versionFrom)))
      case _ => None
    }).filter(_ != version)
  }

  private def versionRange(versionFrom: SemanticVersion, versionTo: SemanticVersion): Constraint = {
    LogicalConstraint(
      List(
        OperatorConstraint(ConstraintOperator.>=, SemanticConstraint(versionFrom)),
        OperatorConstraint(ConstraintOperator.<, SemanticConstraint(versionTo))
      ),
      LogicalOperator.AND,
      " "
    )
  }

  private object VersionRange {
    def unapply(x: Constraint): Option[(SemanticVersion, SemanticVersion)] = x match {
      case LogicalConstraint(
          List(OperatorConstraint(ConstraintOperator.>=, SemanticConstraint(versionFrom), _),
               OperatorConstraint(ConstraintOperator.<, SemanticConstraint(versionTo), _)),
          LogicalOperator.AND,
          _
          ) =>
        Some((versionFrom, versionTo))
      case _ => None
    }
  }
}
