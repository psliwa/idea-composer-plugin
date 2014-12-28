package org.psliwa.idea.composerJson.composer

import scala.io.Source
import scala.util.parsing.json.{JSON, JSONArray, JSONObject}

object Packagist {

  private val PackagistUrl = "https://packagist.org/"

  type Error = String

  def loadPackages(): Either[Error,Seq[String]] = loadPackagesFromPackagist().right.flatMap(loadPackagesFromString)
  def loadVersions(pkg: String): Either[Error,Seq[String]] = loadUri("packages/"+pkg+".json").right.flatMap(loadVersionsFromString)

  protected[composer] def loadPackagesFromPackagist(): Either[Error,String] = loadUri("packages/list.json")
  protected[composer] def loadUri(uri: String): Either[Error,String] = {
    try {
      val in = Source.fromURL(PackagistUrl+uri)
      try {
        Right(in.getLines().mkString)
      } finally {
        in.close()
      }
    } catch {
      case e: Throwable => Left(e.getStackTraceString)
    }
  }

  protected[composer] def loadPackagesFromString(data: String): Either[Error,Seq[String]] = {
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

  protected[composer] def loadVersionsFromString(data: String): Either[Error,Seq[String]] = {
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
