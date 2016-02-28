package org.psliwa.idea.composerJson.composer.version

import org.scalacheck.{Prop, Gen, Properties}
import org.scalacheck.Prop.{forAll, BooleanOperators}
import org.scalatest.PropSpec
import org.scalatest.prop.PropertyChecks

import scala.util.Try

class SemanticVersionTest extends PropSpec with PropertyChecks {

  import VersionGenerators.SemanticVersion._

  property("constructor accepts zeros at beginning") {
    forAll(major, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      Try { SemanticVersion(major, minor) }.isSuccess
    }
  }

  property("constructor does not accept negative numbers on major position") {
    forAll(negative, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      Try { SemanticVersion(major, minor) }.isFailure
    }
  }

  property("constructor does not accept negative numbers on minor and patch positions") {
    forAll(major, minorSome(negative)) { (major: Int, minor: Minor) =>
      Try { SemanticVersion(major, minor) }.isFailure
    }
  }

  property("increment last part") {
    forAll(major, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      val original = SemanticVersion(major, minor)
      val incremented = original.incrementLast

      parts(incremented) == (parts(original).dropRight(1) ++ List(parts(original).last + 1))
    }
  }

  property("drop last part") {
    forAll(major, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      val original = SemanticVersion(major, minor)
      val updated = original.dropLast

      updated.map(parts).getOrElse(List.empty) == parts(original).dropRight(1)
    }
  }

  property("append to version with missing patch part") {
    forAll(major, minorOptional(positiveZero, Gen.const[Patch](None)), positiveZero) { (major: Int, minor: Minor, part: Int) =>
      val original = SemanticVersion(major, minor)
      val updated = original.append(part)

      updated.map(parts).contains(parts(original) ++ List(part))
    }
  }


  property("appending to full semantic version shouldn't be possible") {
    forAll(major, minor(positiveZero, positiveZero.map(Some(_))).map(Some(_)), positiveZero) { (major: Int, minor: Minor, part: Int) =>
      val original = SemanticVersion(major, minor)
      val updated = original.append(part)

      updated.isEmpty
    }
  }

  property("fill missing parts by zeros") {
    forAll(major, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      val original = SemanticVersion(major, minor)
      val updated = original.fillZero

      checkFillZero(original, updated, 3)
    }
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
    forAll(major, minorOptional(positiveZero), Gen.choose(1, 3)) { (major: Int, minor: Minor, minSize: Int) =>
      val original = SemanticVersion(major, minor)
      val updated = original.ensureParts(minSize)

      val size = math.max(minSize, parts(original).size)
      checkFillZero(original, updated, size)
    }
  }

  property("ensure exactly parts - drop parts if necessary") {
    forAll(major, minorOptional(positiveZero), Gen.choose(1, 3)) { (major: Int, minor: Minor, size: Int) =>
      val original = SemanticVersion(major, minor)
      val updated = original.ensureExactlyParts(size)

      if(size >= parts(original).size) checkFillZero(original, updated, size)
      else Prop { parts(original).dropRight(parts(original).size - size) == parts(updated) }
    }
  }

  property("drop zeros when major is positive number") {
    forAll(positive, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      val version = SemanticVersion(major, minor).dropZeros

      parts(version).reverse.dropWhile(_ == 0).reverse == parts(version)
    }
  }

  property("do not drop zero when major is zero and minor is missing") {
    forAll(positiveZero) { (major: Int) =>
      val original = SemanticVersion(major, None)
      val updated = original.dropZeros

      original == updated
    }
  }

  property("number of parts") {
    forAll(positiveZero, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
      val version = SemanticVersion(major, minor)

      version.partsNumber == parts(version).size
    }
  }

  //util functions

  def parts(version: SemanticVersion): List[Int] = version.major :: version.minor.toList ++ version.patch

}
