package org.psliwa.idea.composerJson.json

import org.psliwa.idea.composerJson.util.parsers.JSON

import scala.language.{implicitConversions, postfixOps}
import scala.util.Try
import scala.util.matching.Regex
import spray.json._
import scalaz.Scalaz._

sealed trait Schema

case class SObject(properties: Properties, additionalProperties: Boolean = true) extends Schema {
  lazy val requiredProperties: Map[String, Property] = properties.named.filter(_._2.required)

  def this(properties: Map[String, Property]) = this(new Properties(properties, Map()), true)
  def this(properties: Map[String, Property], additionalProperties: Boolean) =
    this(new Properties(properties, Map()), additionalProperties)
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

final class Properties(val named: Map[String, Property], val patterned: Map[Regex, Property]) {

  private lazy val patternedNamed = patterned.map(property => property._1.toString() -> property._2)

  def get(name: String): Option[Property] = {
    named
      .get(name)
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
      rawJsonElement <- JSON.parse(s)
      jsonObject <- ensureJsonObject(rawJsonElement)
      definitions <- tryDefinitions(jsonObject).orElse(Option(Map[String, Schema]()))
      schema <- jsonTypeToSchema(jsonObject, definitions)
    } yield schema
  }

  private def tryDefinitions(jsonObject: JsObject): Option[Map[String, Schema]] =
    for {
      rawDefinitions <- jsonObject.fields.get("definitions").flatMap(ensureJsonObject)
      resolvedDefinitions = resolveDefinitions(rawDefinitions.fields)
      definitions = parseDefinitions(JsObject(resolvedDefinitions))
    } yield definitions

  private def resolveDefinitions(definitions: Map[String, JsValue]): Map[String, JsValue] = {
    definitions.map { case (name, obj) => name -> resolveDefinition(obj, definitions) }
  }

  // performance is not perfect, because one definition may be resolved many times. If needed: FIXME
  private def resolveDefinition(definition: JsValue, definitions: Map[String, JsValue]): JsValue = {
    def definitionByReference(ref: JsValue): Option[JsValue] =
      for {
        refName <- ensureString(ref)
        name <- resolveRefName(refName)
        definition <- definitions.get(name).map(resolveDefinition(_, definitions))
      } yield definition

    def loop(json: JsValue): JsValue = json match {
      case obj: JsObject if obj.fields.contains("$ref") =>
        obj.fields.get("$ref").flatMap(definitionByReference).getOrElse(obj)
      case obj: JsObject => JsObject(obj.fields.map { case (name, value) => name -> loop(value) })
      case array: JsArray => JsArray(array.elements.map(loop))
      case other => other
    }

    loop(definition)
  }

  implicit private def converterOps[A <: JsValue](c: Converter[A]): ConverterOps[A] = ConverterOps(c)
  private type Converter[A <: JsValue] = (A, Map[String, Schema]) => Option[Schema]

  private def jsonTypeToSchema: Converter[JsValue] =
    (t, defs) =>
      t match {
        case o @ JsObject(_) => jsonObjectToObjectSchema(o, defs)
        case _ => None
      }

  private def ensureJsonObject(t: JsValue): Option[JsObject] = t match {
    case o: JsObject => Some(o)
    case _ => None
  }

  private def parseDefinitions(jsonObject: JsObject): Map[String, Schema] = {
    jsonObject.fields.flatMap {
      case (name, definition) =>
        val maybeSchema = for {
          obj <- ensureJsonObject(definition)
          schema <- jsonObjectToSchema(obj, Map())
        } yield schema

        maybeSchema.map(schema => Map(name -> schema)).getOrElse(Map())
    }
  }

  import OptionOps._

  private def jsonObjectToStringSchema: Converter[JsObject] = (o, _) => {
    for {
      o <- ensureType(o, "string")
      formatValue <- o.fields.get("format").orElse(Some(JsString("any")))
      format <- ensureString(formatValue)
      pattern = Try { o.fields.get("pattern").flatMap(ensureString).map(_.r) }.toOption.flatten
        .map(new PatternFormat(_))
    } yield pattern.map(SString).getOrElse(stringsForFormat.getOrElse(format, SAnyString))
  }

  private def jsonObjectToNumberSchema: Converter[JsObject] = (o, _) => jsonObjectTo(SNumber, "integer")(o)
  private def jsonObjectToBooleanSchema: Converter[JsObject] = (o, _) => jsonObjectTo(SBoolean, "boolean")(o)

  private def jsonObjectTo(s: Schema, t: String)(o: JsObject): Option[Schema] = ensureType(o, t).map(_ => s)
  private def ensureType(o: JsObject, t: String) = o.fields.get("type").filter(_ == JsString(t)).map(_ => o)

  private def jsonObjectToSchema: Converter[JsObject] = (
    jsonObjectToObjectSchema | jsonObjectToStringSchema | jsonObjectToNumberSchema | jsonObjectToBooleanSchema
      | jsonObjectToArraySchema | jsonObjectToEnum | jsonObjectToOr | jsonObjectToPackagesSchema | jsonObjectToFilePathSchema
      | jsonObjectToFilePathsSchema | jsonObjectToRef
  )

  private def jsonObjectToOr: Converter[JsObject] = jsonObjectToComplexOr | jsonObjectToSimpleOr

  private def jsonObjectToComplexOr: Converter[JsObject] = (t, defs) => {
    if (t.fields.contains("oneOf")) {
      for {
        arr <- t.fields.get("oneOf").flatMap(tryJsonArray)
        alternatives <- arr.elements.toList.traverse(tryJsonObject(_).flatMap(jsonObjectToSchema(_, defs)))
      } yield SOr(alternatives)
    } else {
      None
    }
  }

  private def jsonObjectToSimpleOr: Converter[JsObject] = (t, defs) => {
    for {
      arr <- t.fields.get("type").flatMap(tryJsonArray)
      stringValues <- arr.elements.toList.traverse(tryString)
      schemaValues <- stringValues.traverse[Option, Schema](
                       s => jsonObjectToSchema(JsObject(Map("type" -> JsString(s))), defs)
                     )
    } yield SOr(schemaValues)
  }

  private def jsonObjectToEnum: Converter[JsObject] = (t, _) => {
    if (t.fields.contains("enum")) {
      for {
        arr <- t.fields.get("enum").flatMap(tryJsonArray)
        values <- arr.elements.toList.traverse(tryString)
      } yield SStringChoice(values)
    } else {
      None
    }
  }

  private def jsonObjectToRef: Converter[JsObject] = (t, defs) => {
    for {
      ref <- t.fields.get("$ref")
      ref <- ensureString(ref)
      name <- resolveRefName(ref)
      schema <- defs.get(name)
    } yield schema
  }

  private def resolveRefName(ref: String): Option[String] = {
    val prefix = "#/definitions/"
    if (ref.startsWith(prefix)) Option(ref.substring(prefix.length))
    else None
  }

  private def ensureString(a: JsValue) = a match {
    case JsString(x) => Some(x)
    case _ => None
  }

  private def jsonObjectToObjectSchema: Converter[JsObject] = (t, defs) => {
    if (t.fields.get("type").contains(JsString("object"))) {
      val propertiesObject = t.fields.getOrElse("properties", JsObject(Map.empty[String, JsValue]))
      val patternPropertiesObject = t.fields.getOrElse("patternProperties", JsObject(Map.empty[String, JsValue]))

      for {
        properties <- jsonObjectToProperties(propertiesObject, defs)
        patternProperties <- jsonObjectToPatternProperties(patternPropertiesObject, defs)
        additionalProperties <- booleanProperty("additionalProperties")(t.fields).orElse(Some(true))
      } yield SObject(new Properties(properties, patternProperties), additionalProperties)
    } else {
      None
    }
  }

  private def jsonObjectToProperties(maybeJsonObject: JsValue,
                                     defs: Map[String, Schema]): Option[Map[String, Property]] = {
    for {
      jsonObject <- tryJsonObject(maybeJsonObject)
      properties <- jsonObject.fields.traverse(tryProperty(defs))
    } yield properties
  }

  private def tryProperty[K](defs: Map[String, Schema])(jsonObject: JsValue): Option[Property] = jsonObject match {
    case o @ JsObject(_) =>
      for {
        required <- booleanProperty("required")(o.fields).orElse(Some(false))
        description <- stringProperty("description")(o.fields).orElse(Some(""))
        property <- jsonObjectToSchema(o, defs).map(Property(_, required, description))
      } yield property
    case _ => None
  }

  private def jsonObjectToPatternProperties(maybeJsonObject: JsValue,
                                            defs: Map[String, Schema]): Option[Map[Regex, Property]] = {
    for {
      jsonObject <- tryJsonObject(maybeJsonObject)
      properties <- jsonObject.fields.map(prop => prop._1.r -> prop._2).traverse(tryProperty(defs))
    } yield properties
  }

  private def booleanProperty(k: String)(map: Map[String, JsValue]): Option[Boolean] = map.get(k).flatMap(toBoolean)
  private def stringProperty(k: String)(map: Map[String, JsValue]): Option[String] = map.get(k).flatMap {
    case JsString(s) => Some(s)
    case _ => None
  }

  private def toBoolean(o: JsValue): Option[Boolean] = {
    o match {
      case JsBoolean(f) => Some(f)
      case _ => None
    }
  }

  private def jsonObjectToArraySchema: Converter[JsObject] = (t, defs) => {
    if (t.fields.get("type").contains(JsString("array"))) {
      val maybeItems = t.fields.get("items")

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

  private def jsonObjectToPackagesSchema: Converter[JsObject] = (t, _) => {
    if (t.fields.get("type").contains(JsString("packages"))) {
      Some(SPackages)
    } else {
      None
    }
  }

  private def jsonObjectToFilePathSchema: Converter[JsObject] =
    (o, _) => jsonObjectToSchemaWithBooleanArg(SFilePath, "filePath", "existingFilePath")(o)

  private def jsonObjectToSchemaWithBooleanArg(f: Boolean => Schema, typeProp: String, booleanProp: String)(
      jsonObject: JsObject
  ): Option[Schema] = {
    if (jsonObject.fields.get("type").contains(JsString(typeProp))) {
      Some(f(booleanProperty(booleanProp)(jsonObject.fields).getOrElse(true)))
    } else {
      None
    }
  }

  private def jsonObjectToFilePathsSchema: Converter[JsObject] =
    (o, _) => jsonObjectToSchemaWithBooleanArg(SFilePaths, "filePaths", "existingFilePath")(o)

  private object OptionOps {
    def tryJsonObject(a: JsValue): Option[JsObject] = a match {
      case o @ JsObject(_) => Some(o)
      case _ => None
    }

    def tryJsonArray(a: JsValue): Option[JsArray] = a match {
      case a @ JsArray(_) => Some(a)
      case _ => None
    }

    def tryString(a: JsValue): Option[String] = a match {
      case s: JsString => Some(s.value)
      case _ => None
    }
  }

  private case class ConverterOps[A <: JsValue](c: Converter[A]) {
    def |(c2: Converter[A]): Converter[A] = (t, defs) => c(t, defs).orElse(c2(t, defs))
  }
}
