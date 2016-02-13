package org.psliwa.idea.composerJson.composer.parsers

import org.psliwa.idea.composerJson.composer._
import org.psliwa.idea.composerJson.util.TryMonoid
import scala.util.parsing.json.{JSONArray, JSONObject, JSON}
import scala.util.{Success, Failure, Try}
import scalaz._
import Scalaz._

object JsonParsers {

  class ParseException() extends RuntimeException

  private implicit val tryMonoid = new TryMonoid[RepositoryPackages](new ParseException)

  def parsePackageNames(data: String): Try[Seq[String]] = {
    val packages = for {
      result <- JSON.parseRaw(data)
      o <- tryJsonObject(result)
      packageNames <- o.obj.get("packageNames")
      packageNames <- tryJsonArray(packageNames)
    } yield packageNames.list.map(_.toString)

    packages.map(Try(_)).getOrElse(Failure(new ParseException()))
  }

  private def tryJsonObject(a: Any): Option[JSONObject] = a match {
    case a@JSONObject(_) => Some(a)
    case _ => None
  }

  private def tryJsonArray(a: Any): Option[JSONArray] = a match {
    case a@JSONArray(_) => Some(a)
    case _ => None
  }

  private def tryJsonString(a: Any): Option[String] = a match {
    case a: String => Some(a)
    case _ => None
  }

  def parseVersions(data: String): Try[Seq[String]] = {
    val versions = for {
      result <- JSON.parseRaw(data)
      o <- tryJsonObject(result)
      pkg <- o.obj.get("package")
      pkg <- tryJsonObject(pkg)
      versions <- pkg.obj.get("versions")
      versions <- tryJsonObject(versions)
    } yield versions.obj.keys.toList

    versions.map(Try(_)).getOrElse(Failure(new ParseException()))
  }

  def parseLockPackages(data: String): Try[ComposerPackages] = {
    import scalaz.Scalaz._

    def parse(property: String, dev: Boolean) = for {
      result <- JSON.parseRaw(data)
      o <- tryJsonObject(result)
      packagesElement <- o.obj.get(property)
      packagesArray <- tryJsonArray(packagesElement)
      packages <- packagesArray.list.traverse(createLockPackage(dev))
    } yield packages

    val packages = for {
      prodPackages <- parse("packages", dev = false).orElse(Some(List()))
      devPackages <- parse("packages-dev", dev = true).orElse(Some(List()))
    } yield prodPackages ++ devPackages

    packages match {
      case Some(pkgs) if pkgs.nonEmpty => Try(ComposerPackages(pkgs: _*))
      case _ => Failure(new ParseException)
    }
  }

  private def createLockPackage(dev: Boolean)(maybeJsonObject: Any): Option[ComposerPackage] = {
    for {
      jsonObject <- tryJsonObject(maybeJsonObject)
      name <- jsonObject.obj.get("name").map(_.toString)
      version <- jsonObject.obj.get("version").map(_.toString)
      homepage = jsonObject.obj.get("homepage").map(_.toString)
    } yield ComposerPackage(name, version, dev, homepage)
  }

  private def parsePackagesFromPackagesJson(data: String): Try[RepositoryPackages] = {
    def getPackagesFrom(json: Any): Option[Map[String,Seq[String]]] = {
      val packages: Map[String,Seq[String]] = (for {
        obj <- tryJsonObject(json).toList
        packageName <- obj.obj.keys
        packageObject <- obj.obj.get(packageName)
        packageObject <- tryJsonObject(packageObject)
        versions <- Option(packageObject.obj.keys.toSeq)
      } yield packageName -> versions).toMap

      Option(packages)
    }

    val maybeRoot = parse(data)

    val packages = for {
      root <- maybeRoot
      packagesElement <- root.obj.get("packages")
      packages <- getPackagesFrom(packagesElement)
    } yield packages

    val includes = for {
      root <- maybeRoot.toList
      includesElement <- root.obj.get("includes").toList
      includesElement <- tryJsonObject(includesElement).toList
      include <- includesElement.obj.keys.toList
    } yield include

    packages.map(pkgs => Try(RepositoryPackages(pkgs, includes))).getOrElse(tryMonoid.zero)
  }

  private def parse(data: String): Option[JSONObject] = for {
    result <- JSON.parseRaw(data)
    o <- tryJsonObject(result)
  } yield o

  private def parsePackageFromComposerJson(data: String): Try[RepositoryPackages] = {
    (for {
      root <- parse(data)
      name <- root.obj.get("name")
      name <- tryJsonString(name)
      versions <- (for {
        version <- root.obj.get("version")
        version <- tryJsonString(version)
      } yield version).map(Seq(_)).orElse(Some(Seq.empty))
    } yield RepositoryPackages(Map(name -> versions), Seq.empty)).map(Success(_)).getOrElse(tryMonoid.zero)
  }

  def parsePackages(data: String): Try[RepositoryPackages] = parsePackagesFromPackagesJson(data) |+| parsePackageFromComposerJson(data)
}
