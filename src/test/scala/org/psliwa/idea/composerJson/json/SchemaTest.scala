package org.psliwa.idea.composerJson.json

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.json.SchemaConversions._

import scala.language.implicitConversions

class SchemaTest {

  @Test
  def parseEmptyObject(): Unit = {
    assertSchemaEquals(
      new SObject(Map[String, Property](), true),
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
  def parseEmptyObject_additionalPropertiesAreNotAllowed(): Unit = {
    assertSchemaEquals(
      new SObject(Map[String, Property](), false),
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
  def parseSchemaWithOnlyScalarValues(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
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
  def parseSchemaWithRequiredAndDescribedProperties(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map(
          "stringProp" -> Property(SString(), required = true, "some description")
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
  def parseSchemaWithScalarArrayValues(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "arrayNumberProp" -> SArray(SNumber),
          "arrayStringProp" -> SArray(SString()),
          "arrayBooleanProp" -> SArray(SBoolean)
        )
      ),
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
  def parseStringSchemaWithFormat(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "property" -> SString(UriFormat)
        )
      ),
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
  def parseSchemaWithNestedArrayValues(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "arrayNumberProp" -> SArray(SArray(SNumber)),
          "arrayBooleanProp" -> SArray(SArray(SBoolean))
        )
      ),
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
  def parseSchemaWithArrayOfObjects(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "arrayProp" -> SArray(
            new SObject(
              Map[String, Property](
                "stringProp"
              )
            )
          )
        )
      ),
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
  def parseSchemaWithStringChoice(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "enumProp" -> SStringChoice(List("value1", "value2"))
        )
      ),
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
  def parseSchemaWithOr(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "orProp" -> SOr(List(SBoolean, SString()))
        )
      ),
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
  def parseSchemaWithInlineOr(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "orProp" -> SOr(List(SString(), SNumber))
        )
      ),
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
  def parseSchemaWithWildcardObject(): Unit = {
    assertSchemaEquals(
      new SObject(Map[String, Property]()),
      Schema.parse(
        """
          | { "type": "object" }
        """.stripMargin
      )
    )
  }

  @Test
  def parseSchemaWithWildcardArray(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "arrProp" -> SArray(SAny)
        )
      ),
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
  def parsePackagesType(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "require" -> SPackages
        )
      ),
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
  def givenSchemaIsInvalid_expectNone(): Unit = {
    assertEquals(None,
                 Schema.parse(
                   """
        | { "type": "invalid" }
      """.stripMargin
                 ))
  }

  @Test
  def parseSchemaWithPathProperty(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "name" -> SFilePath(existingFilePath = true)
        )
      ),
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
  def parseSchemaWithPathProperty_filePathCouldNotExist(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "name" -> SFilePath(existingFilePath = false)
        )
      ),
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
  def parseSchemaWithRefs_refsShouldBeResolved(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "name" -> new SObject(
            Map[String, Property](
              "street" -> SString(),
              "number" -> SNumber
            )
          )
        )
      ),
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
  def parseSchemaWithRefs_givenInvalidRef_parsingShouldFail(): Unit = {
    assertEquals(
      None,
      Schema.parse(
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
      )
    )
  }

  @Test
  def parseObjectSchema_givenEnumRef_expectedValidObject(): Unit = {
    assertSchemaEquals(
      new SObject(
        Map[String, Property](
          "license" -> SStringChoice(List("a", "b"))
        )
      ),
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
  def parseObjectSchema_givenPatternProperties_expectObjectWithPatternProperties(): Unit = {
    assertSchemaEquals(
      SObject(new Properties(Map(), Map("(.*)".r -> Property(SString(), required = false, "")))),
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

  def assertSchemaEquals(expected: Schema, actual: Option[Schema]): Unit = assertEquals(Some(expected), actual)
}
