package org.psliwa.idea.composerJson.composer.parsers

import org.psliwa.idea.composerJson.composer._
import scala.util.parsing.json.{JSONArray, JSONObject, JSON}

object JsonParsers {

  type Error = String
  
  def parsePackageNames(data: String): Either[Error,Seq[String]] = {
    val packages = for {
      result <- JSON.parseRaw(data)
      o <- tryJsonObject(result)
      packageNames <- o.obj.get("packageNames")
      packageNames <- tryJsonArray(packageNames)
    } yield packageNames.list.map(_.toString)

    packages.map(Right(_)).getOrElse(Left("Json parse error"))
  }

  private def tryJsonObject(a: Any): Option[JSONObject] = a match {
    case a@JSONObject(_) => Some(a)
    case _ => None
  }

  private def tryJsonArray(a: Any): Option[JSONArray] = a match {
    case a@JSONArray(_) => Some(a)
    case _ => None
  }

  def parseVersions(data: String): Either[Error,Seq[String]] = {
    val versions = for {
      result <- JSON.parseRaw(data)
      o <- tryJsonObject(result)
      pkg <- o.obj.get("package")
      pkg <- tryJsonObject(pkg)
      versions <- pkg.obj.get("versions")
      versions <- tryJsonObject(versions)
    } yield versions.obj.keys.toList

    versions.map(Right(_)).getOrElse(Left("Json parse error"))
  }

  def parsePackages(data: String): Either[Error,Packages] = {
    import org.psliwa.idea.composerJson.util.OptionOps._

    def parse(property: String, dev: Boolean) = for {
      result <- JSON.parseRaw(data)
      o <- tryJsonObject(result)
      packagesElement <- o.obj.get(property)
      packagesArray <- tryJsonArray(packagesElement)
      packages <- traverse(packagesArray.list)(createPackage(dev))
    } yield packages

    val packages = for {
      prodPackages <- parse("packages", dev = false).orElse(Some(List()))
      devPackages <- parse("packages-dev", dev = true).orElse(Some(List()))
    } yield prodPackages ++ devPackages

    packages match {
      case Some(pkgs) if pkgs.nonEmpty => Right(Packages(pkgs: _*))
      case _ => Left("Json parse error")
    }
  }

  private def createPackage(dev: Boolean)(maybeJsonObject: Any): Option[Package] = {
    for {
      jsonObject <- tryJsonObject(maybeJsonObject)
      name <- jsonObject.obj.get("name").map(_.toString)
      version <- jsonObject.obj.get("version").map(_.toString)
    } yield Package(name, version, dev)
  }
}
