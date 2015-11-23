package org.psliwa.idea.composerJson.json

import scala.language.{postfixOps, higherKinds, implicitConversions}
import scala.util.matching.Regex
import scala.util.parsing.json.{JSON, JSONArray, JSONObject, JSONType}
import scalaz.Scalaz._

sealed trait Schema

case class SObject(properties: Properties, additionalProperties: Boolean = true) extends Schema {
  lazy val requiredProperties = properties.named.filter(_._2.required)

  def this(properties: Map[String,Property]) = this(new Properties(properties, Map()), true)
  def this(properties: Map[String,Property], additionalProperties: Boolean) = this(new Properties(properties, Map()), additionalProperties)
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

final class Properties(val named: Map[String,Property], val patterned: Map[Regex,Property]) {

  private lazy val patternedNamed = patterned.map(property => property._1.toString() -> property._2)

  def get(name: String): Option[Property] = {
    named.get(name)
      .orElse(patterned.keys.find(_.findFirstIn(name).isDefined).flatMap(patterned.get))
  }
  def +(name: String, property: Property) = new Properties(named + (name -> property), patterned)
  def +(name: Regex, property: Property) = new Properties(named, patterned + (name -> property))

  override def equals(other: Any): Boolean = other match {
    case that: Properties =>
      named == that.named &&
        patternedNamed == that.patternedNamed
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(named, patternedNamed)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

case class Property(schema: Schema, required: Boolean, description: String)

object Schema {

  private val SAnyString = SString(AnyFormat)
  private val SEmailString = SString(EmailFormat)
  private val SUriString = SString(UriFormat)

  private val stringsForFormat = Map("email" -> SEmailString, "uri" -> SUriString)

  def parse(s: String): Option[Schema] = {
    for {
      rawJsonElement <- JSON.parseRaw(s)
      jsonObject <- ensureJsonObject(rawJsonElement)
      definitions <- tryDefinitions(jsonObject).orElse(Option(Map[String,Schema]()))
      schema <- jsonTypeToSchema(jsonObject, definitions)
    } yield schema
  }

  private def tryDefinitions(jsonObject: JSONObject) = for {
    rawDefinitions <- jsonObject.obj.get("definitions")
    definitions <- ensureJsonObject(rawDefinitions).map(parseDefinitions).orElse(Option(Map[String,Schema]()))
  } yield definitions

  private implicit def converterOps[A<:JSONType](c: Converter[A]): ConverterOps[A] = ConverterOps(c)
  private type Converter[A<:JSONType] = (A, Map[String,Schema]) => Option[Schema]

  private def jsonTypeToSchema: Converter[JSONType] = (t, defs) => t match {
    case o@JSONObject(_) => jsonObjectToObjectSchema(o, defs)
    case _ => None
  }

  private def ensureJsonObject(t: Any): Option[JSONObject] = t match {
    case o: JSONObject => Some(o)
    case _ => None
  }

  private def parseDefinitions(jsonObject: JSONObject): Map[String,Schema] = {
    jsonObject.obj.flatMap{ case(name, definition) => {
      val maybeSchema = for {
        obj <- ensureJsonObject(definition)
        schema <- jsonObjectToSchema(obj, Map())
      } yield schema

      maybeSchema.map(schema => Map(name -> schema)).getOrElse(Map())
    }}
  }

  import OptionOps._

  private def jsonObjectToStringSchema: Converter[JSONObject] = (o, defs) => {
    for {
      o <- ensureType(o, "string")
      format <- o.obj.get("format").orElse(Some("any"))
    } yield stringsForFormat.getOrElse(format.toString, SAnyString)
  }

  private def jsonObjectToNumberSchema: Converter[JSONObject] = (o, _) => jsonObjectTo(SNumber, "integer")(o)
  private def jsonObjectToBooleanSchema: Converter[JSONObject] = (o, _) => jsonObjectTo(SBoolean, "boolean")(o)


  private def jsonObjectTo(s: Schema, t: String)(o: JSONObject): Option[Schema] = ensureType(o, t).map(_ => s)
  private def ensureType(o: JSONObject, t: String) = o.obj.get("type").filter(_ == t).map(_ => o)

  private def jsonObjectToSchema: Converter[JSONObject] = (
    jsonObjectToObjectSchema | jsonObjectToStringSchema | jsonObjectToNumberSchema | jsonObjectToBooleanSchema
    | jsonObjectToArraySchema | jsonObjectToEnum | jsonObjectToOr | jsonObjectToPackagesSchema | jsonObjectToFilePathSchema
    | jsonObjectToFilePathsSchema | jsonObjectToRef
  )

  private def jsonObjectToOr: Converter[JSONObject] = jsonObjectToComplexOr | jsonObjectToSimpleOr

  private def jsonObjectToComplexOr: Converter[JSONObject] = (t, defs) => {
    if(t.obj.contains("oneOf")) {
      for {
        arr <- t.obj.get("oneOf").flatMap(tryJsonArray)
        alternatives <- arr.list.traverse(tryJsonObject(_).flatMap(jsonObjectToSchema(_, defs)))
      } yield SOr(alternatives)
    } else {
      None
    }
  }

  private def jsonObjectToSimpleOr: Converter[JSONObject] = (t, defs) => {
    for {
      arr <- t.obj.get("type").flatMap(tryJsonArray)
      stringValues <- arr.list.traverse(tryString)
      schemaValues <- stringValues.traverse[Option,Schema](s => jsonObjectToSchema(JSONObject(Map("type" -> s)), defs))
    } yield SOr(schemaValues)
  }

  private def jsonObjectToEnum: Converter[JSONObject] = (t, defs) => {
    if(t.obj.contains("enum")) {
      for {
        arr <- t.obj.get("enum").flatMap(tryJsonArray)
        values <- arr.list.traverse(tryString)
      } yield SStringChoice(values)
    } else {
      None
    }
  }

  private def jsonObjectToRef: Converter[JSONObject] = (t, defs) => {
    for {
      ref <- t.obj.get("$ref")
      ref <- ensureString(ref)
      name <- refName(ref)
      schema <- defs.get(name)
    } yield schema
  }

  private def refName(ref: String): Option[String] = {
    val prefix = "#/definitions/"
    if(ref.startsWith(prefix)) Option(ref.substring(prefix.length))
    else None
  }

  private def ensureString(a: Any) = a match {
    case x: String => Some(x)
    case _ => None
  }

  private def jsonObjectToObjectSchema: Converter[JSONObject] = (t, defs) => {
    if(t.obj.get("type").contains("object")) {
      val propertiesObject = t.obj.getOrElse("properties", JSONObject(Map()))
      val patternPropertiesObject = t.obj.getOrElse("patternProperties", JSONObject(Map()))

      for {
        properties <- jsonObjectToProperties(propertiesObject, defs)
        patternProperties <- jsonObjectToPatternProperties(patternPropertiesObject, defs)
        additionalProperties <- booleanProperty("additionalProperties")(t.obj).orElse(Some(true))
      } yield SObject(new Properties(properties, patternProperties), additionalProperties)
    } else {
      None
    }
  }

  private def jsonObjectToProperties(maybeJsonObject: Any, defs: Map[String,Schema]): Option[Map[String,Property]] = {
    for {
      jsonObject <- tryJsonObject(maybeJsonObject)
      properties <- jsonObject.obj.traverse(tryProperty(defs))
    } yield properties
  }

  private def tryProperty[K](defs: Map[String,Schema])(jsonObject: Any): Option[Property] = jsonObject match {
    case o@JSONObject(_) => for {
      required <- booleanProperty("required")(o.obj).orElse(Some(false))
      description <- stringProperty("description")(o.obj).orElse(Some(""))
      property <- jsonObjectToSchema(o, defs).map(Property(_, required, description))
    } yield property
    case _ => None
  }

  private def jsonObjectToPatternProperties(maybeJsonObject: Any, defs: Map[String,Schema]): Option[Map[Regex,Property]] = {
    for {
      jsonObject <- tryJsonObject(maybeJsonObject)
      properties <- jsonObject.obj.map(prop => prop._1.r -> prop._2).traverse(tryProperty(defs))
    } yield properties
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

  private def jsonObjectToArraySchema: Converter[JSONObject] = (t, defs) => {
    if(t.obj.get("type").contains("array")) {
      val maybeItems = t.obj.get("items")

      val maybeItemsType = for {
        items <- maybeItems
        rawItems <- tryJsonObject(items)
        itemsType <- jsonObjectToSchema(rawItems, defs)
      } yield itemsType

      Some(SArray(maybeItemsType.getOrElse(SAny)))
    } else {
      None
    }
  }

  private def jsonObjectToPackagesSchema: Converter[JSONObject] = (t, defs) => {
    if(t.obj.get("type").contains("packages")) {
      Some(SPackages)
    } else {
      None
    }
  }

  private def jsonObjectToFilePathSchema: Converter[JSONObject] = (o, _) => jsonObjectToSchemaWithBooleanArg(SFilePath, "filePath", "existingFilePath")(o)

  private def jsonObjectToSchemaWithBooleanArg(f: Boolean => Schema, typeProp: String, booleanProp: String)(jsonObject: JSONObject): Option[Schema] = {
    if(jsonObject.obj.get("type").contains(typeProp)) {
      Some(f(booleanProperty(booleanProp)(jsonObject.obj).getOrElse(true)))
    } else {
      None
    }
  }

  private def jsonObjectToFilePathsSchema: Converter[JSONObject] = (o, _) => jsonObjectToSchemaWithBooleanArg(SFilePaths, "filePaths", "existingFilePath")(o)

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
    def |(c2: Converter[A]): Converter[A] = (t, defs) => c(t, defs).orElse(c2(t, defs))
  }
}