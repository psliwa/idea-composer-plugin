package org.psliwa.idea.composerJson.intellij.codeAssist.schema

import com.intellij.lang.documentation.DocumentationProvider
import org.psliwa.idea.composerJson.intellij.codeAssist.DocumentationTest
import org.junit.Assert._

class SchemaDocumentationProviderTest extends DocumentationTest {
  override protected def documentationProvider: DocumentationProvider = new SchemaDocumentationProvider

  def testGivenPropertyOnFirstLevel_docShouldBeFromSchemaDesc() = {
    checkDocumentation(
      """
        |{
        |  "nam<caret>e": ""
        |}
      """.stripMargin,
      "Package name, including 'vendor-name/' prefix."
    )
  }

  def testGivenNestedProperty_docShouldBeFromSchemaDesc() = {
    checkDocumentation(
      """
        |{
        |  "support": {
        |    "em<caret>ail": ""
        |  }
        |}
      """.stripMargin,
      "Email address for support."
    )
  }

  def testGivenPropertyInArray_docShouldBeFromSchemaDesc() = {
    checkDocumentation(
      """
        |{
        |  "authors": [
        |    {
        |      "na<caret>me": ""
        |    }
        |  ]
        |}
      """.stripMargin,
      "Full name of the author."
    )
  }

  def testGivenPropertyInTopLevel_externalDocUrlShouldExist() = {
    checkDocumentation(
      """
        |{
        |  "nam<caret>e": ""
        |}
      """.stripMargin,
      List("getcomposer.org/doc/04-schema.md#name")
    )
  }

  def testGivenNotComposerJsonFile_docsShouldNotBeFound() = {
    val unexpectedUrl = "getcomposer.org/doc/04-schema.md#name"
    checkDocumentation(
      """
        |{
        |  "nam<caret>e": ""
        |}
      """.stripMargin,
      urls => urls.foreach(url => assertFalse(url.contains(unexpectedUrl))),
      doc => Unit,
      "some.json"
    )
  }
}
