package org.psliwa.idea.composerJson.composer.version

import org.psliwa.idea.composerJson.util.parsers.Parser
import org.psliwa.idea.composerJson.util.parsers.Combinators._
import org.psliwa.idea.composerJson.util.parsers.ImplicitConversions._

object Parser {
  def parse(in: String): Option[Constraint] = run(parser)(in)

  private val dev = string("dev-").flatMap(_ => regex("[a-z0-9]+".r)).map(DevConstraint)
  private val hash = regex("[a-f]{4,40}".r).map(HashConstraint)
  private val semantic = regex("^(\\d+)(\\.\\d+)?(\\.\\d+)?(\\.\\d+)?".r)
    .flatMap(splitIntegers('.'))
    .map(version => SemanticConstraint(new SemanticVersion(version)))

  private def splitIntegers(separator: Char)(in: String): Parser[Array[Int]] = {
    try {
      succeed(in.split(separator).map(_.toInt))
    } catch {
      case e: NumberFormatException => fail()
    }
  }

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
    version <- semantic.map(Some(_)) | succeed(None)
    _ <- "^(\\.)?\\*".r
  } yield WildcardConstraint(version)

  private val wrapped = for {
    prefix <- string("v").map(Some(_)) | succeed(None)
    version <- wildcard | semantic
    suffix <- "^([@\\-]?[a-z0-9]+)".r
  } yield WrappedConstraint(version, prefix, Some(suffix))

  private val primitiveVersion = dev | hash | date | wildcard | wrapped | semantic

  private val alias = for {
    version <- primitiveVersion
    _ <- string(" as ") | string(" AS ")
    as <- primitiveVersion
  } yield AliasedConstraint(version, as)

  private val sortedOperators = ConstraintOperator.values.toList.sortWith(_.toString.length < _.toString.length)
  private val operator = sortedOperators.map(operator => string(operator.toString).map(_ => operator)).foldLeft(fail[ConstraintOperator]())((f, o) => o | f)

  private val operatorVersion = for {
    op <- operator
    version <- primitiveVersion
  } yield OperatorConstraint(op, version)

  private val hyphenRange = for {
    from <- date | semantic
    _ <- ignoreSpaces(string("-"))
    to <- date | semantic
  } yield HyphenRangeConstraint(from, to)

  private val singleVersion = hyphenRange | alias | operatorVersion | primitiveVersion

  private val whiteSpaces = string(" ").many
  private def ignoreSpaces[A](p: Parser[A]): Parser[A] = {
    for {
      _ <- whiteSpaces
      a <- p
      _ <- whiteSpaces
    } yield a
  }

  private val and = for {
    first <- singleVersion
    other <- (ignoreSpaces(string(",")) | string(" ").many1).flatMap(_ => singleVersion).many1
  } yield LogicalConstraint(first::other, LogicalOperator.AND)

  private val or = for {
    first <- and | singleVersion
    other <- ignoreSpaces(string("||")).flatMap(_ => and | singleVersion).many1
  } yield LogicalConstraint(first::other, LogicalOperator.OR)

  private val parser = or | and | singleVersion
}
