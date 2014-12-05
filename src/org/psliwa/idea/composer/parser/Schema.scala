package org.psliwa.idea.composer.parser

import scala.language.{higherKinds, implicitConversions}
import scala.util.parsing.json.{JSONArray, JSONType, JSONObject, JSON}


sealed trait Schema

case class SObject(children: Map[String, Schema]) extends Schema
case class SArray(child: Schema) extends Schema
case class SStringChoice(choices: List[String]) extends Schema
case class SOr(left: Schema, right: Schema) extends Schema

object SBoolean extends Schema
object SString extends Schema
object SNumber extends Schema

object Schema {

  def parse(s: String): Option[Schema] = JSON.parseRaw(s).flatMap(jsonTypeToSchema)

  private implicit def converterOps[A<:JSONType](c: Converter[A]): ConverterOps[A] = ConverterOps(c)
  private type Converter[A<:JSONType] = A => Option[Schema]

  private def jsonTypeToSchema: Converter[JSONType] = {
    case o@JSONObject(_) => jsonObjectToObjectSchema(o)
    case _ => None
  }

  def jsonObjectToStringSchema: Converter[JSONObject] = jsonObjectTo(SString, "string")
  def jsonObjectToNumberSchema: Converter[JSONObject] = jsonObjectTo(SNumber, "integer")
  def jsonObjectToBooleanSchema: Converter[JSONObject] = jsonObjectTo(SBoolean, "boolean")

  def jsonObjectToSchema: Converter[JSONObject] = {
    jsonObjectToObjectSchema | jsonObjectToStringSchema | jsonObjectToNumberSchema | jsonObjectToBooleanSchema | jsonObjectToArraySchema
  }

  import OptionOps._

  def jsonObjectToObjectSchema: Converter[JSONObject] = t => {
    if(t.obj.get("type").exists(_ == "object")) {
      for {
        rawProperties <- tryJsonObject(t.obj.get("properties"))
        properties <- traverseMap(rawProperties.obj){
          case o@JSONObject(_) => jsonObjectToSchema(o)
          case _ => None
        }
      } yield SObject(properties)
    } else {
      None
    }
  }

  def jsonObjectToArraySchema: Converter[JSONObject] = t => {
    if(t.obj.get("type").exists(_ == "array")) {
      for {
        rawItems <- tryJsonObject(t.obj.get("items"))
        itemsType <- jsonObjectToSchema(rawItems)
      } yield SArray(itemsType)
    } else {
      None
    }
  }

  def jsonObjectTo(s: Schema, t: String)(o: JSONObject): Option[Schema] = o.obj.get("type").filter(_ == t).map(_ => s)

  private object OptionOps {
    def traverseMap[K,A,B](as: Map[K,A])(f: A => Option[B]): Option[Map[K,B]] = {
      as.foldLeft(Option(Map[K, B]()))((obs, ka) => map2(f(ka._2), obs)((b, m) => m + (ka._1 -> b)))
    }
    def map2[A,B,C](o1: Option[A], o2: Option[B])(f: (A,B) => C) = o1.flatMap(a => o2.map(b => f(a, b)))

    //TODO
    def tryJsonObject(a: Option[Any]): Option[JSONObject] = a.flatMap {
      case o@JSONObject(_) => Some(o)
      case _ => None
    }

    def tryJsonArray(a: Any): Option[JSONArray] = a match {
      case a@JSONArray(_) => Some(a)
      case _ => None
    }
  }

  case class ConverterOps[A<:JSONType](c: Converter[A]) {
    def |(c2: Converter[A]): Converter[A] = t => c(t).orElse(c2(t))
  }
}