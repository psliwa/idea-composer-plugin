package org.psliwa.idea.composer.test.parser

import org.junit.Test
import org.junit.Assert._
import org.psliwa.idea.composer.parser._
import org.psliwa.idea.composer.test.parser.SchemaConversions._

import scala.language.implicitConversions

class SchemaTest {

  @Test
  def parseEmptySchema() = {
    assertSchemaEquals(
      SObject(Map()),
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
  def parseSchemaWithOnlyScalarValues() = {
    assertSchemaEquals(
      SObject(
        Map(
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
  def parseSchemaWithScalarArrayValues() = {
    assertSchemaEquals(
      SObject(Map(
        "arrayNumberProp" -> SArray(SNumber),
        "arrayStringProp" -> SArray(SString),
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
  def parseSchemaWithNestedArrayValues() = {
    assertSchemaEquals(
      SObject(Map(
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
      SObject(Map(
        "arrayProp" -> SArray(SObject(Map(
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
      SObject(Map(
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
      SObject(Map(
        "orProp" -> SOr(List(SBoolean, SString))
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
      SObject(Map(
        "orProp" -> SOr(List(SString, SNumber))
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
      SObject(Map()),
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
      SObject(Map(
        "arrProp" -> SArray(SObject(Map()))
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

  def assertSchemaEquals(expected: Schema, actual: Option[Schema]) = assertEquals(Some(expected), actual)
}
