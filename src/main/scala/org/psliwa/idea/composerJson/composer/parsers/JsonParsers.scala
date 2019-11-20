package org.psliwa.idea.composerJson.composer.parsers

import org.psliwa.idea.composerJson.composer.model.{PackageDescriptor, PackageName, Packages}
import org.psliwa.idea.composerJson.util.TryMonoid
import org.psliwa.idea.composerJson.util.parsers.JSON
import scalaz.Scalaz._
import spray.json._

import scala.util.{Failure, Success, Try}

object JsonParsers {

  class ParseException() extends RuntimeException

  implicit private val tryMonoid: TryMonoid[RepositoryPackages] = new TryMonoid[RepositoryPackages](new ParseException)

  def parsePackageNames(data: String): Try[Seq[String]] = {
    val packages = for {
      result <- JSON.parse(data)
      o <- tryJsonObject(result)
      packageNames <- o.fields.get("packageNames")
      packageNames <- tryJsonArray(packageNames)
    } yield packageNames.elements.flatMap(tryJsonString)

    packages.map(Try(_)).getOrElse(Failure(new ParseException()))
  }

  private def tryJsonObject(a: JsValue): Option[JsObject] = a match {
    case a @ JsObject(_) => Some(a)
    case _ => None
  }

  private def tryJsonArray(a: JsValue): Option[JsArray] = a match {
    case a @ JsArray(_) => Some(a)
    case _ => None
  }

  private def tryJsonString(a: JsValue): Option[String] = a match {
    case JsString(a) => Some(a)
    case _ => None
  }

  def parseVersions(data: String): Try[Seq[String]] = {
    val versions = for {
      result <- JSON.parse(data)
      o <- tryJsonObject(result)
      pkg <- o.fields.get("package")
      pkg <- tryJsonObject(pkg)
      versions <- pkg.fields.get("versions")
      versions <- tryJsonObject(versions)
    } yield versions.fields.keys.toList

    versions.map(Try(_)).getOrElse(Failure(new ParseException()))
  }

  def parseLockPackages(data: String): Packages = {
    def parse(property: String, dev: Boolean): List[PackageDescriptor] =
      for {
        result <- JSON.parse(data).toList
        o <- tryJsonObject(result).toList
        packagesElement <- o.fields.get(property).toList
        packagesArray <- tryJsonArray(packagesElement).toList
        packageElement <- packagesArray.elements
        pkg <- createLockPackage(dev)(packageElement)
      } yield pkg

    val packages = parse("packages", dev = false) ++ parse("packages-dev", dev = true)

    Packages(packages: _*)
  }

  private def createLockPackage(dev: Boolean)(maybeJsonObject: JsValue): List[PackageDescriptor] = {
    for {
      jsonObject <- tryJsonObject(maybeJsonObject).toList
      packageName <- jsonObject.fields.get("name").flatMap(tryJsonString).toList.map(PackageName)
      version <- jsonObject.fields.get("version").flatMap(tryJsonString).toList
      homepage = jsonObject.fields.get("homepage").flatMap(tryJsonString)
      replaces = jsonObject.fields.get("replace").flatMap(tryJsonObject).map(_.fields.keySet).getOrElse(Set.empty)
      mainPackage = PackageDescriptor(packageName, version, dev, homepage, replacedBy = None)
      packages = mainPackage :: replaces
        .map(PackageName)
        .toList
        .map(PackageDescriptor(_, version, dev, None, Some(mainPackage)))
      pkg <- packages
    } yield pkg
  }

  private def parsePackagesFromPackagesJson(data: String): Try[RepositoryPackages] = {
    def getPackagesFrom(json: JsValue): Option[Map[String, Seq[String]]] = {
      val packages: Map[String, Seq[String]] = (for {
        obj <- tryJsonObject(json).toList
        packageName <- obj.fields.keys
        packageObject <- obj.fields.get(packageName)
        packageObject <- tryJsonObject(packageObject)
        versions <- Option(packageObject.fields.keys.toSeq)
      } yield packageName -> versions).toMap

      Option(packages)
    }

    val maybeRoot = parse(data)

    val packages = for {
      root <- maybeRoot
      packagesElement <- root.fields.get("packages")
      packages <- getPackagesFrom(packagesElement)
    } yield packages

    val includes = for {
      root <- maybeRoot.toList
      includesElement <- root.fields.get("includes").toList
      includesElement <- tryJsonObject(includesElement).toList
      include <- includesElement.fields.keys.toList
    } yield include

    packages.map(pkgs => Try(RepositoryPackages(pkgs, includes))).getOrElse(tryMonoid.zero)
  }

  private def parse(data: String): Option[JsObject] =
    for {
      result <- JSON.parse(data)
      o <- tryJsonObject(result)
    } yield o

  private def parsePackageFromComposerJson(data: String): Try[RepositoryPackages] = {
    (for {
      root <- parse(data)
      name <- root.fields.get("name")
      name <- tryJsonString(name)
      versions <- (for {
                   version <- root.fields.get("version")
                   version <- tryJsonString(version)
                 } yield version).map(Seq(_)).orElse(Some(Seq.empty))
    } yield RepositoryPackages(Map(name -> versions), Seq.empty)).map(Success(_)).getOrElse(tryMonoid.zero)
  }

  def parsePackages(data: String): Try[RepositoryPackages] =
    parsePackagesFromPackagesJson(data) |+| parsePackageFromComposerJson(data)
}
