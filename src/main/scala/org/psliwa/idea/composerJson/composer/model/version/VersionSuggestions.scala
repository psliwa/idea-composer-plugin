package org.psliwa.idea.composerJson.composer.model.version

import org.psliwa.idea.composerJson.composer.model.version.VersionSuggestions.SimplifiedVersionConstraint._
import org.psliwa.idea.composerJson.util.CharOffsetFinder.{ensure, findOffset, findOffsetReverse, not}
import org.psliwa.idea.composerJson.util.OffsetFinder.ImplicitConversions._
import scalaz.Ordering
import scalaz.Scalaz._

import scala.language.postfixOps
import scala.util.Try

object VersionSuggestions {
  private[version] sealed trait SimplifiedVersionConstraint {
    val presentation: String
  }
  private[version] object SimplifiedVersionConstraint {
    sealed trait Semantic extends SimplifiedVersionConstraint {
      val innerVersion: PureSemantic
    }
    case class PureSemantic(semanticVersion: SemanticVersion) extends Semantic {
      val parts: List[Int] = semanticVersion.parts
      val size: Int = parts.size
      val isFull: Boolean = size == 3

      override val innerVersion: PureSemantic = this

      override val presentation: String = parts.mkString(".")
    }
    case class Wildcard(innerVersion: PureSemantic) extends Semantic {
      override val presentation: String = innerVersion.presentation + ".*"
    }
    case class PrefixedSemantic(innerVersion: PureSemantic) extends Semantic {
      override val presentation: String = "v" + innerVersion.presentation
    }
    case class NonSemantic(presentation: String) extends SimplifiedVersionConstraint
  }

  def suggestionsForVersions(versions: Seq[String], prefix: String, mostSignificantFirst: Boolean = true): Seq[String] = {
    versions.flatMap(suggestionsAsConstraintsForVersions(_, prefix))
      .distinct
      .view
      .sortWith((a,b) => mostSignificantFirst == isGreater(a, b))
      .map(_.presentation)
  }

  private[version] def isGreater(a: SimplifiedVersionConstraint, b: SimplifiedVersionConstraint): Boolean = {
    def gt(as: List[Int], bs: List[Int]): Boolean = {
      val compareResult = as.length.compareTo(bs.length)

      if(compareResult > 0) true
      else if(compareResult < 0) false
      else (as ?|? bs) == Ordering.GT
    }

    (a, b) match {
      case (a: PureSemantic, b: PureSemantic) =>
        gt(a.parts, b.parts)
      case (a: Wildcard, b: Wildcard) =>
        gt(a.innerVersion.parts, b.innerVersion.parts)
      case (a: PureSemantic, b: Wildcard) =>
        gt(a.parts, b.innerVersion.parts ++ List(Int.MaxValue))
      case (a: Wildcard, b: PureSemantic) =>
        gt(a.innerVersion.parts ++ List(Int.MaxValue), b.parts)
      case (_: PureSemantic, _) => true
      case (_, _: PureSemantic) => false
      case (_: Wildcard, _) => true
      case (_, _: Wildcard) => false
      case (a, b) =>  a.presentation.compareTo(b.presentation) > 0
    }
  }

  private[version] def parseSemantic(version: String): Option[SimplifiedVersionConstraint.Semantic] = {
    def toInt(s: String): Int = Try { s.toInt }.getOrElse(0)

    "^(v?)(\\d+)\\.(\\d+)\\.(\\d+)$".r.findFirstMatchIn(version).map { semanticMatch =>
      val pureSemanticVersion = PureSemantic(new SemanticVersion(toInt(semanticMatch.group(2)), toInt(semanticMatch.group(3)), toInt(semanticMatch.group(4))))
      if (semanticMatch.group(1) == "") {
        pureSemanticVersion
      } else {
        PrefixedSemantic(pureSemanticVersion)
      }
    }
  }

  private def suggestionsAsConstraintsForVersions(version: String, prefix: String): List[SimplifiedVersionConstraint] = {
    parseSemantic(version) match {
      case Some(semanticVersion) =>
        semanticWildcards(prefix)(semanticVersion)
      case None if !semanticVersionRequired(prefix) =>
        List(NonSemantic(version))
      case None =>
        List.empty
    }
  }

  def suggestionsForVersion(version: String, prefix: String, mostSignificantFirst: Boolean = true): Seq[String] = {
    suggestionsAsConstraintsForVersions(version, prefix)
      .distinct
      .view
      .sortWith((a,b) => mostSignificantFirst == isGreater(a, b))
      .map(_.presentation)
  }

  private def uniqList[A](a: A, b: A): List[A] = if(a == b) List(a) else List(a, b)

  private def suggestionsForSemantic(version: Semantic, providers: List[(PureSemantic,PureSemantic) => List[SimplifiedVersionConstraint]], includeNotNormalized: Boolean = true): List[SimplifiedVersionConstraint] = {
    val normalized = version.innerVersion

    val originalVersions = if(includeNotNormalized) uniqList(version, normalized) else List(normalized)
    val alternativeVersions = List(
      normalized.semanticVersion.copy(other = None),
      normalized.semanticVersion.copy(other = normalized.semanticVersion.other.map { case(minor, _) => minor -> None })
    ).map(version => normalized.copy(semanticVersion = version))

    originalVersions ++ alternativeVersions.flatMap(semVer => providers.flatMap(_(normalized, semVer)))
  }

  private def semanticWildcards(prefix: String)(version: Semantic): List[SimplifiedVersionConstraint] = {
    implicit val text: String = prefix.reverse

    def *(originalVersion: PureSemantic, partialVersion: PureSemantic): List[SimplifiedVersionConstraint] = {
      if(partialVersion.isFull) Nil
      else List(Wildcard(partialVersion))
    }

    def nsr(originalVersion: PureSemantic, partialVersion: PureSemantic): List[SimplifiedVersionConstraint] = {
      if(partialVersion.size == 1 || partialVersion.isFull) Nil
      else List(partialVersion)
    }

    findOffset('^' || '~' || '>' || '<' || '=' || ' ')(0)
      .flatMap(ensure(not(' '))(_))
      .map(_ => suggestionsForSemantic(version, List(nsr), includeNotNormalized = false))
      .getOrElse(suggestionsForSemantic(version, List(*)))
  }

  private def semanticVersionRequired(text: String): Boolean = {
    findOffsetReverse('~' || '^' || '>' || '<' || '=' || ' ')(text.length-1)(text)
      .flatMap(ensure(not(' '))(_)(text))
      .isDefined
  }
}
