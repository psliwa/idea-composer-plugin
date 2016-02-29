package org.psliwa.idea.composerJson.composer.version

import org.scalacheck.Prop.{BooleanOperators, forAll}
import org.scalacheck.{Gen, Prop}
import org.scalatest.PropSpec
import org.scalatest.prop.Checkers

import scala.util.Try

class SemanticVersionTest extends PropSpec with Checkers {

  import VersionGenerators.SemanticVersion._

  def minorOptionalPositive = minorOptional(g = positive, patchGen = patchOptional(positive))
  def minorOptionalZero = minorOptional(g = Gen.const(0), patchGen = patchOptional(Gen.const(0)))

  property("constructor accepts zeros at beginning") {
    check(forAll(major, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      Try { SemanticVersion(major, minor) }.isSuccess
    })
  }

  property("constructor does not accept negative numbers on major position") {
    check(forAll(negative, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      Try { SemanticVersion(major, minor) }.isFailure
    })
  }

  property("constructor does not accept negative numbers on minor and patch positions") {
    check(forAll(major, minorSome(negative)) { (major: Int, minor: Minor) =>
      Try { SemanticVersion(major, minor) }.isFailure
    })
  }

  property("increment last part") {
    check(forAll(major, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      val original = SemanticVersion(major, minor)
      val incremented = original.incrementLast

      parts(incremented) == (parts(original).dropRight(1) ++ List(parts(original).last + 1))
    })
  }

  property("decrement last part") {
    check(forAll(positive, minorOptionalPositive) { (major: Int, minor: Minor) =>
      val original = SemanticVersion(major, minor)
      val decremented = original.decrementLast

      decremented.isDefined &&
        decremented.get.incrementLast == original
    } && forAll(minorOptionalZero) { (minor: Minor) =>
      val original = SemanticVersion(0, minor)
      val decremented = original.decrementLast

      decremented.isEmpty
    })
  }

  property("drop last part") {
    check(forAll(major, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      val original = SemanticVersion(major, minor)
      val updated = original.dropLast

      updated.map(parts).getOrElse(List.empty) == parts(original).dropRight(1)
    })
  }

  property("append to version with missing patch part") {
    check(forAll(major, minorOptional(positiveZero, Gen.const[Patch](None)), positiveZero) { (major: Int, minor: Minor, part: Int) =>
      val original = SemanticVersion(major, minor)
      val updated = original.append(part)

      updated.map(parts).contains(parts(original) ++ List(part))
    })
  }


  property("appending to full semantic version shouldn't be possible") {
    check(forAll(major, minor(positiveZero, positiveZero.map(Some(_))).map(Some(_)), positiveZero) { (major: Int, minor: Minor, part: Int) =>
      val original = SemanticVersion(major, minor)
      val updated = original.append(part)

      updated.isEmpty
    })
  }

  property("fill missing parts by zeros") {
    check(forAll(major, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      val original = SemanticVersion(major, minor)
      val updated = original.fillZero

      checkFillZero(original, updated, 3)
    })
  }

  private def checkFillZero(original: SemanticVersion, updated: SemanticVersion, size: Int) = {
    val sizeIsOk = parts(updated).size == size
    val diffIs0 = parts(original).diff(parts(updated)).forall(_ == 0)
    val addedOnly0 = parts(updated).drop(parts(original).size).forall(_ == 0)

    sizeIsOk    :| "size == #3" &&
    diffIs0     :| "difference is only 0" &&
    addedOnly0  :| "only zeros were added"
  }

  property("ensure parts") {
    check(forAll(major, minorOptional(positiveZero), Gen.choose(1, 3)) { (major: Int, minor: Minor, minSize: Int) =>
      val original = SemanticVersion(major, minor)
      val updated = original.ensureParts(minSize)

      val size = math.max(minSize, parts(original).size)
      checkFillZero(original, updated, size)
    })
  }

  property("ensure exactly parts - drop parts if necessary") {
    check(forAll(major, minorOptional(positiveZero), Gen.choose(1, 3)) { (major: Int, minor: Minor, size: Int) =>
      val original = SemanticVersion(major, minor)
      val updated = original.ensureExactlyParts(size)

      if(size >= parts(original).size) checkFillZero(original, updated, size)
      else Prop { parts(original).dropRight(parts(original).size - size) == parts(updated) }
    })
  }

  property("drop zeros when major is positive number") {
    check(forAll(positive, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      val version = SemanticVersion(major, minor).dropZeros

      parts(version).reverse.dropWhile(_ == 0).reverse == parts(version)
    })
  }

  property("do not drop zero when major is zero and minor is missing") {
    check(forAll(positiveZero) { (major: Int) =>
      val original = SemanticVersion(major, None)
      val updated = original.dropZeros

      original == updated
    })
  }

  property("number of parts") {
    check(forAll(positiveZero, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      val version = SemanticVersion(major, minor)

      version.partsNumber == parts(version).size
    })
  }

  property("the same version is equal") {
    check(forAll(positiveZero, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      val version = SemanticVersion(major, minor)

      version.compareTo(version) == 0
    })
  }

  property("version is smaller than version with incremented last part") {
    check(forAll(positiveZero, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      val version = SemanticVersion(major, minor)

      version < version.incrementLast
    })
  }

  property("the version is smaller when has less parts and the last part of the second version is not 0") {
    check(forAll(positiveZero, positive) { (major: Int, minor: Int) =>
      val version = SemanticVersion(major, Some((minor, None)))

      version.dropLast.get < version
    })
  }

  property("the version is equal to the second version when they are the same expect tailing zeros") {
    check(forAll(positiveZero, positiveZero) { (major: Int, minor: Int) =>
      val version = SemanticVersion(major, Some((minor, None)))

      version.compareTo(version.fillZero) == 0
    })
  }

  //util functions

  def parts(version: SemanticVersion): List[Int] = version.major :: version.minor.toList ++ version.patch
  def last(a: SemanticVersion) = parts(a).last

}
