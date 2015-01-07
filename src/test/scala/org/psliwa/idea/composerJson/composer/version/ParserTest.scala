package org.psliwa.idea.composerJson.composer.version

import org.junit.Assert._
import org.junit.Test

class ParserTest {
  @Test
  def parseDevMasterConstraint() = {
    assertConstraintEquals(
      DevConstraint("master"),
      "dev-master"
    )
  }

  @Test
  def parseDevTrunkConstraint() = {
    assertConstraintEquals(
      DevConstraint("trunk"),
      "dev-trunk"
    )
  }

  @Test
  def parseFullWildcardConstraint() = {
    assertConstraintEquals(
      WildcardConstraint(None),
      "*"
    )
  }

  @Test
  def parseHashConstraint() = {
    assertConstraintEquals(
      HashConstraint("afafaf"),
      "afafaf"
    )
  }

  @Test
  def parseSemanticConstraint() = {
    assertConstraintEquals(
      SemanticConstraint(new SemanticVersion(1, 2, 3)),
      "1.2.3"
    )
  }

  @Test
  def parseSemanticConstraintWithLongParts() = {
    assertConstraintEquals(
      SemanticConstraint(new SemanticVersion(123, 234, 345)),
      "123.234.345"
    )
  }

  @Test
  def parseDateConstraint_formatYMDWithoutSeparators() = {
    assertConstraintEquals(
      DateConstraint("20101012"),
      "20101012"
    )
  }

  @Test
  def parseDateConstraint_formatYMDHisWithoutSeparators() = {
    assertConstraintEquals(
      DateConstraint("20101012203020"),
      "20101012203020"
    )
  }

  @Test
  def parseDateConstraint_formatYMDHis() = {
    assertConstraintEquals(
      DateConstraint("20101012-203020"),
      "20101012-203020"
    )
  }

  @Test
  def parseDateConstraint_formatYM() = {
    List("2010.10", "2010-10").foreach(version => assertConstraintEquals(DateConstraint(version), version))
  }

  @Test
  def parseDateConstraint_formatYMD() = {
    List("2010.10.12", "2010-10-12").foreach(version => assertConstraintEquals(DateConstraint(version), version))
  }

  @Test
  def parseAlias() = {
    List(" as ", " AS ").foreach(as => {
      assertConstraintEquals(
        AliasedConstraint(DevConstraint("master"), SemanticConstraint(new SemanticVersion(1, 2, 0)), as),
        "dev-master"+as+"1.2.0"
      )
    })
  }

  @Test
  def parseWildcardedSemVer() = {
    assertConstraintEquals(
      WildcardConstraint(Some(SemanticConstraint(new SemanticVersion(1, 2)))),
      "1.2.*"
    )
  }

  @Test
  def parseWrappedSemVer() = {
    assertConstraintEquals(
      WrappedConstraint(SemanticConstraint(new SemanticVersion(1, 2)), Some("v"), Some("@dev")),
      "v1.2@dev"
    )
  }

  @Test
  def parseOperatorConstraint() = {
    ConstraintOperator.values.foreach(operator => {
      assertConstraintEquals(
        OperatorConstraint(operator, SemanticConstraint(new SemanticVersion(1, 2))),
        operator+"1.2"
      )
    })
  }

  @Test
  def parseLogicalAndConstraint() = {
    List(",", ", ", " ").foreach(separator => {
      assertConstraintEquals(
        LogicalConstraint(List(DevConstraint("master"), DevConstraint("trunk")), LogicalOperator.AND, separator),
        "dev-master"+separator+"dev-trunk"
      )
    })
  }

  @Test
  def parseHyphenRangeConstraint() = {
    List("-", " - ").foreach(separator => {
      assertConstraintEquals(
        HyphenRangeConstraint(SemanticConstraint(new SemanticVersion(1, 2)), SemanticConstraint(new SemanticVersion(2, 0)), separator),
        "1.2"+separator+"2.0"
      )
    })
  }

  @Test
  def parseNumberOutOfIntegerRange_resultShouldBeNone() = {
    assertEquals(None, Parser.parse("0231231231231231246131253124113"))
  }

  @Test
  def parseLogicalOrConstraint() = {
    List("||", " || ", "|", " | ").foreach(separator => {
      assertConstraintEquals(
        LogicalConstraint(List(DevConstraint("master"), DevConstraint("trunk")), LogicalOperator.OR, separator),
        "dev-master"+separator+"dev-trunk"
      )
    })
  }

  @Test
  def parseLogicalConstraint_andHasGreaterPrecedenceThanOr() = {
    assertConstraintEquals(
      LogicalConstraint(
        List(
          DevConstraint("master"),
          LogicalConstraint(List(DevConstraint("trunk"), DevConstraint("abc")), LogicalOperator.AND, ", ")
        ),
        LogicalOperator.OR,
        " || "
      ),
      "dev-master || dev-trunk, dev-abc"
    )
  }

  @Test
  def parseLogicalConstraint_andHasGreaterPrecedenceThanOr_inversedOrder() = {
    assertConstraintEquals(
      LogicalConstraint(
        List(
          LogicalConstraint(List(DevConstraint("trunk"), DevConstraint("abc")), LogicalOperator.AND, ", "),
          DevConstraint("master")
        ),
        LogicalOperator.OR,
        " || "
      ),
      "dev-trunk, dev-abc || dev-master"
    )
  }

  private def assertConstraintEquals(expected: Constraint, actual: String) = {
    assertEquals(s"parsing: '$actual'", Some(expected), Parser.parse(actual))
  }
}
