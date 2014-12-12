package org.psliwa.idea.composer.packagist

import scala.io.Source
import scala.util.parsing.json.{JSONArray, JSONObject, JSON}

object Packagist {

  private val packagistUrl = "https://packagist.org/"

  type Error = String

  def loadPackages(): Either[Error,List[String]] = loadPackagesFromPackagist().right.flatMap(loadPackagesFromString)
  def loadVersions(pkg: String): Either[Error,List[String]] = loadUri("packages/"+pkg+".json").right.flatMap(loadVersionsFromString)

  protected[packagist] def loadPackagesFromPackagist(): Either[Error,String] = loadUri("packages/list.json")
  protected[packagist] def loadUri(uri: String): Either[Error,String] = {
    try {
      val in = Source.fromURL(packagistUrl+uri)
      try {
        Right(in.getLines().mkString)
      } finally {
        in.close()
      }
    } catch {
      case e: Throwable => Left(e.getStackTraceString)
    }
  }

  protected[packagist] def loadPackagesFromString(data: String): Either[Error,List[String]] = {
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

  protected[packagist] def loadVersionsFromString(data: String): Either[Error,List[String]] = {
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
}
