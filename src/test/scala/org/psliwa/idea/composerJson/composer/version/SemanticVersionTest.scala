package org.psliwa.idea.composerJson.composer.version

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.forAll

import scala.util.Try

class SemanticVersionTest extends Properties("SemanticVersion") {

  type Patch = Option[(Int)]
  type Minor = Option[(Int,Patch)]

  //generators
  val positiveZero = Gen.choose[Int](0, 20)
  val positive = positiveZero.map(_ + 1)
  val negative = Gen.choose[Int](-20, -1)
  val major = positiveZero
  def patchOptional(g: Gen[Int]): Gen[Patch] = Gen.option[(Int)](g)
  def minorOptional(g: Gen[Int], patchGen: Gen[Patch] = patchOptional(positiveZero)): Gen[Minor] = Gen.option(minor(g, patchGen))
  def minorSome(g: Gen[Int]): Gen[Minor] = minor(g).map(Some(_))
  def minor(g: Gen[Int], patchGen: Gen[Patch] = patchOptional(positiveZero)) = for {
    m <- g
    p <- patchGen
  } yield (m, p)

  //properties

  property("accepts zeros at beginning") = forAll(major, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
    SemanticVersion(major, minor)
    true
  }

  property("does not accept negative numbers") = forAll(negative, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
    Try { SemanticVersion(major, minor) }.isFailure
  } && forAll(major, minorSome(negative)) { (major: Int, minor: Minor) =>
    Try { SemanticVersion(major, minor) }.isFailure
  }

  property("incrementLast") = forAll(major, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
    val original = SemanticVersion(major, minor)
    val incremented = original.incrementLast

    parts(incremented) == (parts(original).dropRight(1) ++ List(parts(original).last + 1))
  }

  property("dropLast") = forAll(major, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
    val original = SemanticVersion(major, minor)
    val updated = original.dropLast

    updated.map(parts).getOrElse(List.empty) == parts(original).dropRight(1)
  }

  property("append") = forAll(major, minorOptional(positiveZero, Gen.const[Patch](None)), positiveZero) { (major: Int, minor: Minor, part: Int) =>
    val original = SemanticVersion(major, minor)
    val updated = original.append(part)

    updated.map(parts).contains(parts(original) ++ List(part))
  } && forAll(major, minor(positiveZero, positiveZero.map(Some(_))).map(Some(_)), positiveZero) { (major: Int, minor: Minor, part: Int) =>
    val original = SemanticVersion(major, minor)
    val updated = original.append(part)

    updated.isEmpty
  }

  property("fillZero") = forAll(major, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
    val original = SemanticVersion(major, minor)
    val updated = original.fillZero

    checkFillZero(original, updated, 3)
  }

  private def checkFillZero(original: SemanticVersion, updated: SemanticVersion, size: Int) = {
    val sizeIs3 = parts(updated).size == size
    val diffIs0 = parts(original).diff(parts(updated)).forall(_ == 0)
    val addedOnly0 = parts(updated).drop(parts(original).size).forall(_ == 0)

    sizeIs3 && diffIs0 && addedOnly0
  }

  property("ensureParts") = forAll(major, minorOptional(positiveZero), Gen.choose(1, 3)) { (major: Int, minor: Minor, minSize: Int) =>
    val original = SemanticVersion(major, minor)
    val updated = original.ensureParts(minSize)

    val size = math.max(minSize, parts(original).size)
    checkFillZero(original, updated, size)
  }

  property("ensureExactlyParts") = forAll(major, minorOptional(positiveZero), Gen.choose(1, 3)) { (major: Int, minor: Minor, size: Int) =>
    val original = SemanticVersion(major, minor)
    val updated = original.ensureExactlyParts(size)

    if(size >= parts(original).size) checkFillZero(original, updated, size)
    else parts(original).dropRight(parts(original).size - size) == parts(updated)
  }

  property("dropZeros") = forAll(positive, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
    val version = SemanticVersion(major, minor).dropZeros

    parts(version).reverse.dropWhile(_ == 0).reverse == parts(version)
  } && forAll(positiveZero) { (major: Int) =>
    val original = SemanticVersion(major, None)
    val updated = original.dropZeros

    original == updated
  }

  property("partsNumber") = forAll(positiveZero, minorOptional(positiveZero)) { (major: Int, minor: Minor) =>
    val version = SemanticVersion(major, minor)

    version.partsNumber == parts(version).size
  }

  //util functions

  def parts(version: SemanticVersion): List[Int] = version.major :: version.minor.toList ++ version.patch

}
