package org.psliwa.idea.composerJson.composer.version.ConstraintTest

import org.psliwa.idea.composerJson.composer.version._
import org.scalacheck.{Prop, Gen, Properties}
import Prop.BooleanOperators

class ParsePresentationTest extends Properties("Constraint") {

  property("presentation should be able to be parsed") = Prop.forAll(versionGen) { (version: Constraint) =>
    val parsed = Parser.parse(version.presentation)

    parsed.contains(version) :| s"$parsed contains $version, presentation ${version.presentation}"
  }

  //generators
  def numberGen = Gen.choose(0, 20)
  def semanticVersionGen = for {
    size <- Gen.choose(1, 3)
    parts <- Gen.listOfN(size, numberGen)
  } yield SemanticConstraint(new SemanticVersion(parts.toArray))
  def devVersionGen = Gen.alphaStr.suchThat(_.length > 0).map(_.toLowerCase).map(DevConstraint)
  def wildcardVersionGen = Gen.option(semanticVersionGen).map(WildcardConstraint)
  def wrappedVersionGen = for {
    prefix <- Gen.option(Gen.const("v"))
    separator <- Gen.oneOf("@", "-")
    suffix <- Gen.alphaStr.map(_.toLowerCase).map(s => separator+s).suchThat(_.length > 1)
    version <- semanticVersionGen
  } yield WrappedConstraint(version, prefix, Some(suffix))
  def operatorGen = Gen.oneOf(ConstraintOperator.!=, ConstraintOperator.<, ConstraintOperator.<=, ConstraintOperator.>, ConstraintOperator.>=, ConstraintOperator.^, ConstraintOperator.~)
  def paddingGen = Gen.oneOf("", " ")
  def operatorVersionGen = for {
    version <- primitiveVersionGen
    operator <- operatorGen
    padding <- paddingGen
  } yield OperatorConstraint(operator, version, padding)
  def hashGen = for {
    n <- Gen.choose(4, 40)
    chars <- Gen.listOfN(n, Gen.oneOf('a', 'b', 'c', 'd', 'e', 'f'))
  } yield chars.mkString("")
  def hashVersionGen = hashGen.map(HashConstraint)
  def hyphenRangeVersionGen = for {
    version1 <- semanticVersionGen
    version2 <- semanticVersionGen
  } yield HyphenRangeConstraint(version1, version2)
  def aliasedVersionGen = for {
    version1 <- primitiveVersionGen
    version2 <- primitiveVersionGen
    separator <- Gen.oneOf(" as ", " AS ")
  } yield AliasedConstraint(version1, version2, separator)
  def logicalOperatorGen = Gen.oneOf((LogicalOperator.AND, ","), (LogicalOperator.AND, ", "), (LogicalOperator.OR, "|"), (LogicalOperator.OR, " || "))
  def logicalVersionGen = for {
    count <- Gen.choose(2, 5)
    versions <- Gen.listOfN(count, singleVersionGen)
    (operator, presentation) <- logicalOperatorGen
  } yield LogicalConstraint(versions, operator, presentation)

  def primitiveVersionGen = Gen.oneOf(devVersionGen, hashVersionGen, wrappedVersionGen, wildcardVersionGen, semanticVersionGen)
  def singleVersionGen = Gen.oneOf(primitiveVersionGen, aliasedVersionGen, operatorVersionGen, hyphenRangeVersionGen)
  def versionGen = Gen.oneOf(semanticVersionGen, devVersionGen, wildcardVersionGen, wrappedVersionGen, operatorVersionGen, hashVersionGen, hyphenRangeVersionGen, aliasedVersionGen, logicalVersionGen)

}
