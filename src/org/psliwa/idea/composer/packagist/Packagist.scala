package org.psliwa.idea.composer.packagist

import scala.io.Source
import scala.util.parsing.json.{JSONArray, JSONObject, JSONType, JSON}

object Packagist {

  private val packagistUrl = "https://packagist.org/"

  type Error = String

  def load(): Either[Error,List[String]] = loadFromPackagist().right.flatMap(loadFromString)

  protected[packagist] def loadFromPackagist(): Either[Error,String] = loadFromPackagist("packages/list.json")
  protected[packagist] def loadFromPackagist(uri: String): Either[Error,String] = {
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

  protected[packagist] def loadFromString(data: String): Either[Error,List[String]] = {
    JSON.parseRaw(data).map(mapJsonType).getOrElse(Left("Json parse error"))
  }

  private def mapJsonType(o: JSONType): Either[Error,List[String]] = o match {
    case JSONObject(props) => {
      props.get("packageNames").map(mapJsonArray).getOrElse(Left("\"packageNames\" key is missing"))
    }
    case _ => Left("Expected JSONObject")
  }

  private def mapJsonArray(a: Any): Either[Error,List[String]] = a match {
    case JSONArray(as) => Right(as.map(_.toString))
    case _ => Left("Expected JSONArray")
  }
}
