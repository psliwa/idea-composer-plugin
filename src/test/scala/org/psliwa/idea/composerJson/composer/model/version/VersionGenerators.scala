package org.psliwa.idea.composerJson.composer.model.version

import org.scalacheck.Gen

/**
  * Generators for tests based on ScalaCheck
  */
object VersionGenerators {
  def semanticVersion(size: Int): Gen[SemanticConstraint] =
    Gen
      .listOfN(size, validSemanticVersionNumber)
      .map(parts => SemanticConstraint(new SemanticVersion(parts.toArray)))
  def semanticVersion: Gen[SemanticConstraint] =
    for {
      size <- Gen.choose(1, 3)
      version <- semanticVersion(size)
    } yield version
  def validSemanticVersionNumber: Gen[Int] = Gen.choose(0, 20)
  def devVersion: Gen[Constraint] = Gen.alphaStr.suchThat(_.length > 0).map(_.toLowerCase).map(DevConstraint)
  def wildcardVersion = Gen.option(semanticVersion).map(WildcardConstraint)
  def wrappedVersion(size: Int) =
    for {
      prefix <- Gen.option(Gen.const("v"))
      separator <- Gen.oneOf("@", "-")
      suffix <- Gen.alphaStr.map(_.toLowerCase).map(s => separator + s).suchThat(_.length > 1)
      version <- semanticVersion(size)
    } yield WrappedConstraint(version, prefix, Some(suffix))
  def wrappedVersion: Gen[Constraint] =
    for {
      size <- Gen.choose(1, 3)
      version <- wrappedVersion(size)
    } yield version
  def operator =
    Gen.oneOf(ConstraintOperator.!=,
              ConstraintOperator.<,
              ConstraintOperator.<=,
              ConstraintOperator.>,
              ConstraintOperator.>=,
              ConstraintOperator.^,
              ConstraintOperator.~)
  def nsrOperator = Gen.oneOf(ConstraintOperator.^, ConstraintOperator.~)
  def padding = Gen.oneOf("", " ")
  def operatorVersion =
    for {
      version <- primitiveVersion
      operator <- operator
      padding <- padding
    } yield OperatorConstraint(operator, version, padding)
  def hash =
    for {
      n <- Gen.choose(4, 40)
      chars <- Gen.listOfN(n, Gen.oneOf('a', 'b', 'c', 'd', 'e', 'f'))
    } yield chars.mkString("")
  def hashVersion = hash.map(HashConstraint)
  def hyphenRangeVersion =
    for {
      version1 <- semanticVersion
      version2 <- semanticVersion
    } yield HyphenRangeConstraint(version1, version2)
  def aliasedVersion =
    for {
      version1 <- primitiveVersion
      version2 <- primitiveVersion
      separator <- Gen.oneOf(" as ", " AS ")
    } yield AliasedConstraint(version1, version2, separator)
  def logicalOperator =
    Gen.oneOf((LogicalOperator.AND, ","),
              (LogicalOperator.AND, ", "),
              (LogicalOperator.OR, "|"),
              (LogicalOperator.OR, " || "))
  def logicalVersion =
    for {
      count <- Gen.choose(2, 5)
      versions <- Gen.listOfN(count, singleVersion)
      (operator, presentation) <- logicalOperator
    } yield LogicalConstraint(versions, operator, presentation)
  def constraintOperator =
    Gen.oneOf(ConstraintOperator.!=,
              ConstraintOperator.<,
              ConstraintOperator.<=,
              ConstraintOperator.>,
              ConstraintOperator.>=,
              ConstraintOperator.^,
              ConstraintOperator.~)
  def primitiveVersion = Gen.oneOf(devVersion, hashVersion, wrappedVersion, wildcardVersion, semanticVersion)
  def singleVersion = Gen.oneOf(primitiveVersion, aliasedVersion, operatorVersion, hyphenRangeVersion)
  def constraintVersion =
    for {
      operator <- constraintOperator
      version <- semanticVersion
      padding <- padding
    } yield OperatorConstraint(operator, version, padding)
  def version =
    Gen.oneOf(
      semanticVersion,
      constraintVersion,
      devVersion,
      wildcardVersion,
      wrappedVersion,
      operatorVersion,
      hashVersion,
      hyphenRangeVersion,
      aliasedVersion,
      logicalVersion
    )

  object SemanticVersion {

    type Patch = Option[(Int)]
    type Minor = Option[(Int, Patch)]

    def positiveZero = Gen.choose[Int](0, 20)
    def positive = positiveZero.map(_ + 1)
    def negative = Gen.choose[Int](-20, -1)
    def major = positiveZero
    def patchOptional(g: Gen[Int]): Gen[Patch] = Gen.option[(Int)](g)
    def minorOptional(g: Gen[Int], patchGen: Gen[Patch] = patchOptional(positiveZero)): Gen[Minor] =
      Gen.option(minor(g, patchGen))
    def minorSome(g: Gen[Int]): Gen[Minor] = minor(g).map(Some(_))
    def minor(g: Gen[Int], patchGen: Gen[Patch] = patchOptional(positiveZero)) =
      for {
        m <- g
        p <- patchGen
      } yield (m, p)
  }
}
