package org.psliwa.idea.composer.version

import org.psliwa.idea.composer.util.CharType._
import org.psliwa.idea.composer.util.CharType.ImplicitConversions._

object Version {

  type SemanticVersion = String

  def alternativesForPrefix(prefix: String)(version: String): List[String] = {
    trySemantic(version)
      .map(semanticWildcards(prefix))
      .orElse(
        Some(List(version))
          .filter(_ => !semanticVersionRequired(prefix))
      )
      .getOrElse(List())
  }

  private def semanticVersionRequired(text: String) = {
    findOffsetReverse('~' || '^' || ' ')(text.length-1)(text)
      .flatMap(ensure(not(' '))(_)(text))
      .isDefined
  }

  private def trySemantic(s: String): Option[SemanticVersion] = "^v?(\\d)(\\.\\d)+$".r.findFirstIn(s)

  private def semanticWildcards(prefix: String)(version: SemanticVersion): List[String] = {

    implicit val text = prefix.reverse

    val * = Wildcards.asterix _
    val tilde = Wildcards.tilde _
    val peak = Wildcards.peak _

    findOffset('~' || ' ')(0)
      .flatMap(ensure('~')(_))
      .map(_ => alternativesForSemantic(version, List(tilde), includeOriginal = false))
      .orElse(
        findOffset('^' || ' ')(0)
          .flatMap(ensure('^')(_))
          .map(_ => alternativesForSemantic(version, List(peak)))
      )
      .getOrElse(alternativesForSemantic(version, List(*, tilde, peak)))
  }

  private def alternativesForSemantic(version: SemanticVersion, providers: List[(String,List[String]) => List[String]], includeOriginal: Boolean = true): List[String] = {
    val normalized = if (version(0) == 'v') version.drop(1) else version

    val originalVersions = if(includeOriginal) uniqList(version, normalized) else List()

    originalVersions ++ normalized.split("\\.")
      .foldLeft(List[List[String]]())((aas, a) => {
        aas match {
          case Nil => List(a) :: aas
          case h::_ => (a :: h) :: aas
        }
      })
      .map(_.reverse)
      .flatMap(semVer => providers.flatMap(_(normalized, semVer)))
  }

  private def uniqList(a: String, b: String): List[String] = if(a == b) List(a) else List(a, b)

  private object Wildcards {

    def asterix(version: String, subVersion: List[String]): List[String] = {
      if(version.count(_ == '.') + 1 == subVersion.length) Nil
      else List(subVersion.mkString(".")+".*")
    }

    def tilde(version: String, subVersion: List[String]): List[String] = {
      if(subVersion.length == 1 || subVersion.length > 2) Nil
      else List(subVersion.mkString("."))
    }

    def peak(version: String, subVersion: List[String]): List[String] = {
      if(subVersion.length == 1 || version.count(_ == '.') + 1 == subVersion.length) Nil
      else List(subVersion.mkString("."))
    }
  }
}
