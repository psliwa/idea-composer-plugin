package org.psliwa.idea.composerJson.composer.model.version

import org.psliwa.idea.composerJson.util.parsers.{Parser, Parsers}
import org.psliwa.idea.composerJson.util.parsers.ParserMonad._
import org.psliwa.idea.composerJson.util.parsers.Implicits._

object Parser {
  def parse(in: String): Option[Constraint] = parser.run(in)

  private val dev = string("dev-").flatMap(_ => regex("[a-z0-9]+".r)).map(DevConstraint)
  private val hash = regex("^[a-f]{4,40}".r).map(HashConstraint)
  private val semantic = regex("^(\\d+)(\\.\\d+)?(\\.\\d+)?".r)
    .flatMap(splitIntegers('.'))
    .map(version => SemanticConstraint(new SemanticVersion(version)))

  private def splitIntegers(separator: Char)(in: String): Parser[Array[Int]] = {
    try {
      succeed(in.split(separator).map(_.toInt))
    } catch {
      case _: NumberFormatException => fail()
    }
  }

  private val whiteSpaces = " ".many.map(_.mkString)
  private val dateSeparator = "^(\\.|\\-)".r | succeed("")
  private val datePart = "^\\d{2}".r | succeed("")

  private val date = for {
    year <- "^[1-9]\\d{3}".r
    s1 <- dateSeparator
    month <- "^\\d{2}".r
    s2 <- dateSeparator
    day <- datePart
    s3 <- dateSeparator
    hour <- datePart
    s4 <- dateSeparator
    minute <- datePart
    s5 <- dateSeparator
    seconds <- datePart
  } yield DateConstraint(year + s1 + month + s2 + day + s3 + hour + s4 + minute + s5 + seconds)

  private val wildcard = for {
    version <- semantic.map(Some(_): Option[SemanticConstraint]) | succeed(None: Option[SemanticConstraint])
    _ <- "^(\\.)?\\*".r
  } yield WildcardConstraint(version)

  private val wrapped = for {
    prefix <- string("v").map(Some(_): Option[String]) | succeed(None: Option[String])
    version <- wildcard | semantic
    suffix <- "^([@\\-]?[a-z0-9]+)".r
  } yield WrappedConstraint(version, prefix, Some(suffix))

  private val primitiveVersion = dev | hash | date | wrapped | wildcard | semantic

  private val alias = for {
    version <- primitiveVersion
    separator <- string(" as ") | string(" AS ")
    as <- primitiveVersion
  } yield AliasedConstraint(version, as, separator)

  private val sortedOperators = ConstraintOperator.values.toList.sortWith(_.toString.length < _.toString.length)
  private val operator = sortedOperators
    .map(operator => string(operator.toString).map(_ => operator))
    .foldLeft(fail[ConstraintOperator]())((f, o) => o | f)

  private val operatorVersion = for {
    op <- operator
    separator <- whiteSpaces
    version <- primitiveVersion
  } yield OperatorConstraint(op, version, separator)

  private val hyphenRange = for {
    from <- date | semantic
    separator <- spaceWrapper("-")
    to <- date | semantic
  } yield HyphenRangeConstraint(from, to, separator)

  private val singleVersion = hyphenRange | alias | operatorVersion | primitiveVersion

  private def spaceWrapper(p: Parser[String]): Parser[String] = {
    for {
      prefix <- whiteSpaces
      value <- p
      suffix <- whiteSpaces
    } yield prefix + value + suffix
  }

  private val andSeparator: Parser[String] = spaceWrapper("," | "")
  private val orSeparator: Parser[String] = spaceWrapper("||" | "|")

  private val and = for {
    first <- singleVersion
    separator <- andSeparator
    second <- singleVersion
    other <- andSeparator.flatMap(_ => singleVersion).many
  } yield LogicalConstraint(first :: second :: other, LogicalOperator.AND, separator)

  private val or = for {
    first <- and | singleVersion
    separator <- orSeparator
    second <- and | singleVersion
    other <- orSeparator.flatMap(_ => and | singleVersion).many
  } yield LogicalConstraint(first :: second :: other, LogicalOperator.OR, separator)

  private def guard[A](value: A) = Parsers.whole().flatMap(s => if (s.isEmpty) succeed(value) else fail())
  private val parser = (or | and | singleVersion).flatMap(guard)
}
