package org.psliwa.idea.composerJson.composer.version

import org.psliwa.idea.composerJson.util.CharOffsetFinder._
import org.psliwa.idea.composerJson.util.OffsetFinder.ImplicitConversions._

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

  def isGreater(version1: String, version2: String): Boolean = {
    def isSemVer(v: String): Boolean = !v.exists(_.isLetter)
    val version1IsSemVer = isSemVer(version1)
    val version2IsSemVer = isSemVer(version2)

    if(version1IsSemVer && !version2IsSemVer) true
    else if(!version1IsSemVer && version2IsSemVer) false
    else if(version1IsSemVer && version2IsSemVer) {
      val dotsCount1 = version1.count(_ == '.')
      val dotsCount2 = version2.count(_ == '.')

      if(dotsCount1 == dotsCount2) {
        version1.replace("*", "999999").compareTo(version2.replace("*", "999999")) >= 0
      } else {
        dotsCount1 >= dotsCount2
      }
    }
    else version1.compareTo(version2) >= 0
  }

  private def semanticVersionRequired(text: String) = {
    findOffsetReverse('~' || '^' || '>' || '<' || '=' || ' ')(text.length-1)(text)
      .flatMap(ensure(not(' '))(_)(text))
      .isDefined
  }

  private def trySemantic(s: String): Option[SemanticVersion] = "^v?(\\d)(\\.\\d)+$".r.findFirstIn(s)

  private def semanticWildcards(prefix: String)(version: SemanticVersion): List[String] = {

    implicit val text = prefix.reverse

    val * = Wildcards.asterix _
    val nsr = Wildcards.nsr _

    findOffset('^' || '~' || '>' || '<' || '=' || ' ')(0)
      .flatMap(ensure(not(' '))(_))
      .map(_ => alternativesForSemantic(version, List(nsr), includeNotNormalized = false))
      .getOrElse(alternativesForSemantic(version, List(*)))
  }

  private def alternativesForSemantic(version: SemanticVersion, providers: List[(String,List[String]) => List[String]], includeNotNormalized: Boolean = true): List[String] = {
    val normalized = if (version(0) == 'v') version.drop(1) else version

    val originalVersions = if(includeNotNormalized) uniqList(version, normalized) else List(normalized)

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

    def nsr(version: String, subVersion: List[String]): List[String] = {
      if(subVersion.length == 1 || version.count(_ == '.') + 1 == subVersion.length) Nil
      else List(subVersion.mkString("."))
    }
  }
}
