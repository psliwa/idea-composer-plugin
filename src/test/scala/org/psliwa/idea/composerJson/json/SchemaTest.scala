package org.psliwa.idea.composerJson.json

import org.junit.Assert._
import org.junit.Test

import scala.language.implicitConversions
import SchemaConversions._

import scala.util.matching.Regex

class SchemaTest {

  @Test
  def parseEmptyObject() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](), true),
      Schema.parse(
        """
          {
            "type":"object",
            "properties":{}
          }
        """
      )
    )
  }

  @Test
  def parseEmptyObject_additionalPropertiesAreNotAllowed() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](), false),
      Schema.parse(
        """
          {
            "type":"object",
            "properties":{},
            "additionalProperties": false
          }
        """
      )
    )
  }

  @Test
  def parseSchemaWithOnlyScalarValues() = {
    assertSchemaEquals(
      new SObject(
        Map[String,Property](
          "stringProp",
          "numberProp" -> SNumber,
          "booleanProp" -> SBoolean
        )
      ),
      Schema.parse("""
        {
         "type": "object",
         "properties": {
           "stringProp": {"type":"string"},
           "numberProp": {"type":"integer"},
           "booleanProp": {"type":"boolean"}
         }
        }
      """)
    )
  }

  @Test
  def parseSchemaWithRequiredAndDescribedProperties() = {
    assertSchemaEquals(
      new SObject(
        Map(
          "stringProp" -> Property(SString(), required=true, "some description")
        )
      ),
      Schema.parse("""
        {
         "type": "object",
         "properties": {
           "stringProp": {"type":"string", "required": true, "description": "some description" }
         }
        }
       """)
    )
  }

  @Test
  def parseSchemaWithScalarArrayValues() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "arrayNumberProp" -> SArray(SNumber),
        "arrayStringProp" -> SArray(SString()),
        "arrayBooleanProp" -> SArray(SBoolean)
      )),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "properties": {
          |     "arrayNumberProp": {
          |       "type": "array",
          |       "items": { "type": "integer" }
          |     },
          |     "arrayStringProp": {
          |       "type": "array",
          |       "items": { "type": "string" }
          |     },
          |     "arrayBooleanProp": {
          |       "type": "array",
          |       "items": { "type": "boolean" }
          |     }
          |   }
          | }
        """.stripMargin
      )
    )
  }

  @Test
  def parseStringSchemaWithFormat() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "property" -> SString(UriFormat)
      )),
      Schema.parse(
        """
          |{
          |  "type": "object",
          |  "properties": {
          |    "property": { "type": "string", "format": "uri" }
          |  }
          |}
        """.stripMargin
      )
    )
  }

  @Test
  def parseSchemaWithNestedArrayValues() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "arrayNumberProp" -> SArray(SArray(SNumber)),
        "arrayBooleanProp" -> SArray(SArray(SBoolean))
      )),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "properties": {
          |     "arrayNumberProp": {
          |       "type": "array",
          |       "items": {
          |         "type": "array",
          |         "items": {
          |           "type": "integer"
          |         }
          |       }
          |     },
          |     "arrayBooleanProp": {
          |       "type": "array",
          |       "items": {
          |         "type": "array",
          |         "items": {
          |           "type": "boolean"
          |         }
          |       }
          |     }
          |   }
          | }
        """.stripMargin
      )
    )
  }


  @Test
  def parseSchemaWithArrayOfObjects() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "arrayProp" -> SArray(new SObject(Map[String,Property](
          "stringProp"
        )))
      )),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "properties": {
          |     "arrayProp": {
          |       "type": "array",
          |       "items": {
          |         "type": "object",
          |         "properties": {
          |           "stringProp": {
          |             "type": "string"
          |           }
          |         }
          |       }
          |     }
          |   }
          | }
        """.stripMargin
      )
    )
  }

  @Test
  def parseSchemaWithStringChoice() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "enumProp" -> SStringChoice(List("value1", "value2"))
      )),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "properties": {
          |     "enumProp": {
          |       "enum": [ "value1", "value2" ]
          |     }
          |   }
          | }
        """.stripMargin
      )
    )
  }

  @Test
  def parseSchemaWithOr() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "orProp" -> SOr(List(SBoolean, SString()))
      )),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "properties": {
          |     "orProp": {
          |       "oneOf": [
          |         { "type": "boolean" },
          |         { "type": "string" }
          |       ]
          |     }
          |   }
          | }
        """.stripMargin
      )
    )
  }

  @Test
  def parseSchemaWithInlineOr() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "orProp" -> SOr(List(SString(), SNumber))
      )),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "properties": {
          |     "orProp": {
          |       "type": ["string", "integer" ]
          |     }
          |   }
          | }
        """.stripMargin
      )
    )
  }

  @Test
  def parseSchemaWithWildcardObject() = {
    assertSchemaEquals(
      new SObject(Map[String,Property]()),
      Schema.parse(
        """
          | { "type": "object" }
        """.stripMargin
      )
    )
  }

  @Test
  def parseSchemaWithWildcardArray() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "arrProp" -> SArray(SAny)
      )),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "properties": {
          |     "arrProp": { "type": "array" }
          |   }
          | }
        """.stripMargin
      )
    )
  }

  @Test
  def parsePackagesType() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "require" -> SPackages
      )),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "properties": {
          |     "require": { "type": "packages" }
          |   }
          | }
        """.stripMargin
      )
    )
  }

  @Test
  def givenSchemaIsInvalid_expectNone() = {
    assertEquals(None, Schema.parse(
      """
        | { "type": "invalid" }
      """.stripMargin
    ))
  }

  @Test
  def parseSchemaWithPathProperty() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "name" -> SFilePath(existingFilePath = true)
      )),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "properties": {
          |     "name": { "type": "filePath" }
          |   }
          | }
        """.stripMargin
      )
    )
  }

  @Test
  def parseSchemaWithPathProperty_filePathCouldNotExist() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "name" -> SFilePath(existingFilePath = false)
      )),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "properties": {
          |     "name": { "type": "filePath", "existingFilePath": false }
          |   }
          | }
        """.stripMargin
      )
    )
  }

  @Test
  def parseSchemaWithRefs_refsShouldBeResolved() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "name" -> new SObject(Map[String,Property](
          "street" -> SString(),
          "number" -> SNumber
        ))
      )),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "properties": {
          |     "name": { "$ref": "#/definitions/address" }
          |   },
          |   "definitions": {
          |     "address": {
          |       "type": "object",
          |       "properties": {
          |         "street": { "type": "string" },
          |         "number": { "type": "integer" }
          |       }
          |     }
          |   }
          | }
        """.stripMargin
      )
    )
  }

  @Test
  def parseSchemaWithRefs_givenInvalidRef_parsingShouldFail() = {
    assertEquals(None, Schema.parse(
      """
        | {
        |   "type": "object",
        |   "properties": {
        |     "name": { "$ref": "#/definitions/invalid" }
        |   },
        |   "definitions": {
        |     "address": {
        |       "type": "object",
        |       "properties": {
        |         "street": { "type": "string" },
        |         "number": { "type": "integer" }
        |       }
        |     }
        |   }
        | }
      """.stripMargin
    ))
  }

  @Test
  def parseObjectSchema_givenEnumRef_expectedValidObject() = {
    assertSchemaEquals(
      new SObject(Map[String,Property](
        "license" -> SStringChoice(List("a", "b"))
      )),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "properties": {
          |     "license": { "$ref": "#/definitions/license" }
          |   },
          |   "definitions": {
          |     "license": { "enum": [ "a", "b" ] }
          |   }
          | }
        """.stripMargin
      )
    )
  }

  @Test
  def parseObjectSchema_givenPatternProperties_expectObjectWithPatternProperties() = {
    assertSchemaEquals(
      new SObject(new Properties(Map(), Map("(.*)".r -> Property(SString(), required = false, "")))),
      Schema.parse(
        """
          | {
          |   "type": "object",
          |   "patternProperties": {
          |     "(.*)": { "type": "string" }
          |   }
          | }
        """.stripMargin
      )
    )
  }

  def assertSchemaEquals(expected: Schema, actual: Option[Schema]) = assertEquals(Some(expected), actual)
}
