package org.psliwa.idea.composer.schema

import scala.language.{higherKinds, implicitConversions}
import scala.util.parsing.json.{JSONArray, JSONType, JSONObject, JSON}

sealed trait Schema

case class SObject(children: Map[String, Schema]) extends Schema
case class SArray(child: Schema) extends Schema
case class SStringChoice(choices: List[String]) extends Schema
case class SOr(alternatives: List[Schema]) extends Schema

object SBoolean extends Schema
object SString extends Schema
object SNumber extends Schema
object SPackages extends Schema
object SFilePath extends Schema

object Schema {

  def parse(s: String): Option[Schema] = JSON.parseRaw(s).flatMap(jsonTypeToSchema)

  private implicit def converterOps[A<:JSONType](c: Converter[A]): ConverterOps[A] = ConverterOps(c)
  private type Converter[A<:JSONType] = A => Option[Schema]

  private def jsonTypeToSchema: Converter[JSONType] = {
    case o@JSONObject(_) => jsonObjectToObjectSchema(o)
    case _ => None
  }

  import OptionOps._

  private def jsonObjectToStringSchema: Converter[JSONObject] = jsonObjectTo(SString, "string")
  private def jsonObjectToNumberSchema: Converter[JSONObject] = jsonObjectTo(SNumber, "integer")
  private def jsonObjectToBooleanSchema: Converter[JSONObject] = jsonObjectTo(SBoolean, "boolean")

  private def jsonObjectTo(s: Schema, t: String)(o: JSONObject): Option[Schema] = o.obj.get("type").filter(_ == t).map(_ => s)

  private def jsonObjectToSchema: Converter[JSONObject] = (
    jsonObjectToObjectSchema | jsonObjectToStringSchema | jsonObjectToNumberSchema | jsonObjectToBooleanSchema
    | jsonObjectToArraySchema | jsonObjectToEnum | jsonObjectToOr | jsonObjectToPackagesSchema | jsonObjectToPathSchema
  )

  private def jsonObjectToOr: Converter[JSONObject] = jsonObjectToComplexOr | jsonObjectToSimpleOr

  private def jsonObjectToComplexOr: Converter[JSONObject] = t => {
    if(t.obj.contains("oneOf")) {
      for {
        arr <- t.obj.get("oneOf").flatMap(tryJsonArray)
        alternatives <- traverse(arr.list)(tryJsonObject(_).flatMap(jsonObjectToSchema))
      } yield SOr(alternatives)
    } else {
      None
    }
  }

  private def jsonObjectToSimpleOr: Converter[JSONObject] = t => {
    for {
      arr <- t.obj.get("type").flatMap(tryJsonArray)
      stringValues <- traverse(arr.list)(tryString)
      schemaValues <- traverse(stringValues)(s => jsonObjectToSchema(JSONObject(Map("type" -> s))))
    } yield SOr(schemaValues)
  }

  private def jsonObjectToEnum: Converter[JSONObject] = t => {
    if(t.obj.contains("enum")) {
      for {
        arr <- t.obj.get("enum").flatMap(tryJsonArray)
        values <- traverse(arr.list)(tryString)
      } yield SStringChoice(values)
    } else {
      None
    }
  }

  private def jsonObjectToObjectSchema: Converter[JSONObject] = t => {
    if(t.obj.get("type").exists(_ == "object")) {
      val maybeJsonObject = t.obj.getOrElse("properties", JSONObject(Map()))

      for {
        jsonObject <- tryJsonObject(maybeJsonObject)
        properties <- traverseMap(jsonObject.obj){
          case o@JSONObject(_) => jsonObjectToSchema(o)
          case _ => None
        }
      } yield SObject(properties)
    } else {
      None
    }
  }

  private def jsonObjectToArraySchema: Converter[JSONObject] = t => {
    if(t.obj.get("type").exists(_ == "array")) {
      val maybeJsonObject = t.obj.getOrElse("items", JSONObject(Map("type" -> "object")))

      for {
        rawItems <- tryJsonObject(maybeJsonObject)
        itemsType <- jsonObjectToSchema(rawItems)
      } yield SArray(itemsType)
    } else {
      None
    }
  }

  private def jsonObjectToPackagesSchema: Converter[JSONObject] = t => {
    if(t.obj.get("type").exists(_ == "packages")) {
      Some(SPackages)
    } else {
      None
    }
  }

  private def jsonObjectToPathSchema: Converter[JSONObject] = t => {
    if(t.obj.get("type").exists(_ == "path")) {
      Some(SFilePath)
    } else {
      None
    }
  }

  private object OptionOps {
    def traverseMap[K,A,B](as: Map[K,A])(f: A => Option[B]): Option[Map[K,B]] = {
      as.foldLeft(Option(Map[K, B]()))((obs, ka) => map2(f(ka._2), obs)((b, m) => m + (ka._1 -> b)))
    }

    def traverse[A,B](as: List[A])(f: A => Option[B]): Option[List[B]] = {
      as.foldLeft(Option(List[B]()))((obs, a) => map2(obs, f(a))(_ :+ _))
    }

    def map2[A,B,C](o1: Option[A], o2: Option[B])(f: (A,B) => C) = o1.flatMap(a => o2.map(b => f(a, b)))

    def tryJsonObject(a: Any): Option[JSONObject] = a match {
      case o@JSONObject(_) => Some(o)
      case _ => None
    }

    def tryJsonArray(a: Any): Option[JSONArray] = a match {
      case a@JSONArray(_) => Some(a)
      case _ => None
    }

    def tryString(a: Any): Option[String] = a match {
      case s: String => Some(s)
      case _ => None
    }
  }

  private case class ConverterOps[A<:JSONType](c: Converter[A]) {
    def |(c2: Converter[A]): Converter[A] = t => c(t).orElse(c2(t))
  }
}