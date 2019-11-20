package org.psliwa.idea.composerJson.composer.model.version

import org.psliwa.idea.composerJson.BasePropSpec
import org.psliwa.idea.composerJson.composer.model.version.{VersionGenerators => gen}
import org.scalacheck.{Gen, Prop}
import org.scalacheck.Prop.{forAll, propBoolean}

import scala.language.implicitConversions

class VersionNsrEquivalentsTest extends BasePropSpec {

  def semanticVersionGen: Gen[SemanticConstraint] = gen.semanticVersion(size = 3)

  property("pure semantic versions should not have equivalents") {
    forAll(semanticVersionGen) { version: Constraint =>
      equivalentsFor(version).isEmpty
    }
  }

  property("next Significant Releases should have range equivalents") {
    forAll(gen.semanticVersion, gen.nsrOperator) {
      (semanticVersion: SemanticConstraint, operator: ConstraintOperator) =>
        val equivalents = equivalentsFor(OperatorConstraint(operator, semanticVersion))
        val version = semanticVersion.version

        equivalents match {
          case List(equivalent) =>
            contains(equivalent, operator, version)
          case _ => Prop { false }
        }
    }
  }

  private def contains(rangeVersion: Constraint, nsrOperator: ConstraintOperator, version: SemanticVersion): Prop = {
    import ConstraintOperator._
    import LogicalOperator._

    val versionBeforeLastIncremented = version.dropLast.getOrElse(version).incrementLast

    rangeVersion match {
      case LogicalConstraint(
          List(OperatorConstraint(_, SemanticConstraint(start), _), OperatorConstraint(_, SemanticConstraint(end), _)),
          AND,
          _
          ) =>
        val range = Range(start, end)

        val generalProperties =
          range.contains(version)                           :| s"original version is in range: $range" &&
            range.contains(version.`[*.*.*]`.incrementLast) :| s"version with incremented patch is in range: $range"

        val `~ properties` = (nsrOperator == ConstraintOperator.~) ==> (
          !range.contains(versionBeforeLastIncremented)                          :| s"version with incremented before last part is out of range: $range" &&
            ((last(version) > 0) ==> !range.contains(version.decrementLast.get)) :| s"version with decremented last part is out of range: $range"
        )

        val `^ properties` = (nsrOperator == ConstraintOperator.^) ==> (
          version.`[*]?` ==> range.contains(version.`[*.*]`.incrementLast) :| s"version with incremented minor is in range: $range" &&
            !range.contains(version.incrementMajor)                        :| s"version with incremented before last part is in range: $range" &&
            (version.major == 0 && version.`[*.*.]?`) ==>
              (range.contains(version.`[*.*.*]`.incrementLast)
                && !range.contains(version.`[*.*]`.incrementLast)) :| s"pre 1.0 release has special behaviour"
        )

        generalProperties && (`~ properties` ++ `^ properties`)
      case _ => Prop { false }
    }
  }

  private def equivalentsFor(constraint: Constraint) = VersionEquivalents.equivalentsFor(constraint)
  def last(a: SemanticVersion): Int = (a.patch.toList ++ a.minor.toList ++ List(a.major)).head
  case class Range[T <: Ordered[T]](start: T, end: T) {
    def contains(value: T): Boolean = value >= start && value < end
  }

  implicit class SemanticVersionOps(val version: SemanticVersion) {
    def `[*.*.*]` : SemanticVersion = version.ensureExactlyParts(3)
    def `[*.*]` : SemanticVersion = version.ensureExactlyParts(2)
    def `[*]` : SemanticVersion = version.ensureExactlyParts(1)
    def `[*.]` : SemanticVersion = version.ensureParts(1)
    def `[*.*.]` : SemanticVersion = version.ensureParts(2)

    def `[*]?` : Boolean = version.partsNumber == 1
    def `[*.*]?` : Boolean = version.partsNumber == 2
    def `[*.*.]?` : Boolean = version.partsNumber >= 2
    def `[*.*.*]?` : Boolean = version.partsNumber == 3
  }

  implicit def unwrapSemanticVersion(wrapper: SemanticVersionOps): SemanticVersion = wrapper.version
}
