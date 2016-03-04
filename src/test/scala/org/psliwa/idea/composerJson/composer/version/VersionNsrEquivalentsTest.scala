package org.psliwa.idea.composerJson.composer.version

import org.psliwa.idea.composerJson.BasePropSpec
import org.psliwa.idea.composerJson.composer.version.{VersionGenerators => gen}
import org.scalacheck.Prop
import org.scalacheck.Prop.{BooleanOperators, forAll}

import scala.language.implicitConversions

class VersionNsrEquivalentsTest extends BasePropSpec {

  def semanticVersionGen = gen.semanticVersion(size = 3)

  property("pure semantic versions should not have equivalents") {
    forAll(semanticVersionGen) { (version: Constraint) =>
      equivalentsFor(version).isEmpty
    }
  }

  property("next Significant Releases should have range equivalents") {
    forAll(gen.semanticVersion, gen.nsrOperator) { (semanticVersion: SemanticConstraint, operator: ConstraintOperator) =>
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
      case LogicalConstraint(List(OperatorConstraint(>=, SemanticConstraint(start), _), OperatorConstraint(<, SemanticConstraint(end), _)), AND, _) =>
        val range = Range(start, end)

        val generalProperties =
          range.contains(version)                                                 :| s"original version is in range: $range" &&
            range.contains(version.`[*.*.*]`.incrementLast)                       :| s"version with incremented patch is in range: $range"

        val `~ properties` = (nsrOperator == ConstraintOperator.~) ==> (
          !range.contains(versionBeforeLastIncremented)                           :| s"version with incremented before last part is out of range: $range" &&
            ((last(version) > 0) ==> !range.contains(version.decrementLast.get))  :| s"version with decremented last part is out of range: $range"
          )

        val `^ properties` = (nsrOperator == ConstraintOperator.^) ==> (
          version.`[*]?` ==> range.contains(version.`[*.*]`.incrementLast)        :| s"version with incremented minor is in range: $range" &&
            !range.contains(version.incrementMajor)                               :| s"version with incremented before last part is in range: $range" &&
            (version.major == 0 && version.`[*.*.]?`) ==>
              (range.contains(version.`[*.*.*]`.incrementLast)
                && !range.contains(version.`[*.*]`.incrementLast))                :| s"pre 1.0 release has special behaviour"
          )

        generalProperties && (`~ properties` ++ `^ properties`)
      case _ => Prop { false }
    }
  }

  private def equivalentsFor(constraint: Constraint) = Version.equivalentsFor(constraint)
  def last(a: SemanticVersion) = (a.patch.toList ++ a.minor.toList ++ List(a.major)).head
  case class Range[T <: Ordered[T]](start: T, end: T) {
    def contains(value: T): Boolean = value >= start && value < end
  }

  implicit class SemanticVersionOps(val version: SemanticVersion) {
    def `[*.*.*]` = version.ensureExactlyParts(3)
    def `[*.*]` = version.ensureExactlyParts(2)
    def `[*]` = version.ensureExactlyParts(1)
    def `[*.]` = version.ensureParts(1)
    def `[*.*.]` = version.ensureParts(2)

    def `[*]?` = version.partsNumber == 1
    def `[*.*]?` = version.partsNumber == 2
    def `[*.*.]?` = version.partsNumber >= 2
    def `[*.*.*]?` = version.partsNumber == 3
  }

  implicit def unwrapSemanticVersion(wrapper: SemanticVersionOps): SemanticVersion = wrapper.version
}
