package org.psliwa.idea.composerJson.json

import scala.language.{higherKinds, implicitConversions}
import scala.util.parsing.json.{JSON, JSONArray, JSONObject, JSONType}
import org.psliwa.idea.composerJson.util.OptionOps._

sealed trait Schema

case class SObject(properties: Map[String, Property], additionalProperties: Boolean = true) extends Schema {
  lazy val requiredProperties = properties.filter(_._2.required)
}
case class SArray(child: Schema) extends Schema
case class SStringChoice(choices: List[String]) extends Schema
case class SOr(alternatives: List[Schema]) extends Schema
case class SString(format: Format = AnyFormat) extends Schema
case class SFilePath(existingFilePath: Boolean = true) extends Schema
case class SFilePaths(existingFilePath: Boolean = true) extends Schema

object SBoolean extends Schema
object SNumber extends Schema
object SPackages extends Schema
object SAny extends Schema

case class Property(schema: Schema, required: Boolean, description: String)

object Schema {

  private val SAnyString = SString(AnyFormat)
  private val SEmailString = SString(EmailFormat)
  private val SUriString = SString(UriFormat)

  private val stringsForFormat = Map("email" -> SEmailString, "uri" -> SUriString)

  def parse(s: String): Option[Schema] = JSON.parseRaw(s).flatMap(jsonTypeToSchema)

  private implicit def converterOps[A<:JSONType](c: Converter[A]): ConverterOps[A] = ConverterOps(c)
  private type Converter[A<:JSONType] = A => Option[Schema]

  private def jsonTypeToSchema: Converter[JSONType] = {
    case o@JSONObject(_) => jsonObjectToObjectSchema(o)
    case _ => None
  }

  import OptionOps._

  private def jsonObjectToStringSchema: Converter[JSONObject] = (o) => {
    for {
      o <- ensureType(o, "string")
      format <- o.obj.get("format").orElse(Some("any"))
    } yield stringsForFormat.getOrElse(format.toString, SAnyString)
  }

  private def jsonObjectToNumberSchema: Converter[JSONObject] = jsonObjectTo(SNumber, "integer")
  private def jsonObjectToBooleanSchema: Converter[JSONObject] = jsonObjectTo(SBoolean, "boolean")


  private def jsonObjectTo(s: Schema, t: String)(o: JSONObject): Option[Schema] = ensureType(o, t).map(_ => s)
  private def ensureType(o: JSONObject, t: String) = o.obj.get("type").filter(_ == t).map(_ => o)

  private def jsonObjectToSchema: Converter[JSONObject] = (
    jsonObjectToObjectSchema | jsonObjectToStringSchema | jsonObjectToNumberSchema | jsonObjectToBooleanSchema
    | jsonObjectToArraySchema | jsonObjectToEnum | jsonObjectToOr | jsonObjectToPackagesSchema | jsonObjectToFilePathSchema
    | jsonObjectToFilePathsSchema
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
          case o@JSONObject(_) => for{
            required <- booleanProperty("required")(o.obj).orElse(Some(false))
            description <- stringProperty("description")(o.obj).orElse(Some(""))
            schema <- jsonObjectToSchema(o).map(Property(_, required, description))
          } yield schema
          case _ => None
        }
        additionalProperties <- booleanProperty("additionalProperties")(t.obj).orElse(Some(true))
      } yield SObject(properties, additionalProperties)
    } else {
      None
    }
  }

  private def booleanProperty(k: String)(map: Map[String,Any]): Option[Boolean] = map.get(k).flatMap(toBoolean)
  private def stringProperty(k: String)(map: Map[String,Any]): Option[String] = map.get(k).map(_.toString)

  private def toBoolean(o: Any): Option[Boolean] = {
    try {
      Some(o.toString.toBoolean)
    } catch {
      case _: IllegalArgumentException => None
    }
  }

  private def jsonObjectToArraySchema: Converter[JSONObject] = t => {
    if(t.obj.get("type").exists(_ == "array")) {
      val maybeItems = t.obj.get("items")

      val maybeItemsType = for {
        items <- maybeItems
        rawItems <- tryJsonObject(items)
        itemsType <- jsonObjectToSchema(rawItems)
      } yield itemsType

      Some(SArray(maybeItemsType.getOrElse(SAny)))
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

  private def jsonObjectToFilePathSchema = jsonObjectToSchemaWithBooleanArg(SFilePath, "filePath", "existingFilePath") _

  private def jsonObjectToSchemaWithBooleanArg(f: Boolean => Schema, typeProp: String, booleanProp: String)(jsonObject: JSONObject): Option[Schema] = {
    if(jsonObject.obj.get("type").exists(_ == typeProp)) {
      Some(f(booleanProperty(booleanProp)(jsonObject.obj).getOrElse(true)))
    } else {
      None
    }
  }

  private def jsonObjectToFilePathsSchema = jsonObjectToSchemaWithBooleanArg(SFilePaths, "filePaths", "existingFilePath") _

  private object OptionOps {
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