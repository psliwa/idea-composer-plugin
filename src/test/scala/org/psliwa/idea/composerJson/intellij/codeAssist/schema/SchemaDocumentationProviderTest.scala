package org.psliwa.idea.composerJson.intellij.codeAssist.schema

import com.intellij.lang.documentation.DocumentationProvider
import org.psliwa.idea.composerJson.intellij.codeAssist.DocumentationTest
import org.junit.Assert._
import org.psliwa.idea.composerJson._

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

  def testGivenFileWithNewLine_thereShouldNotBeNullPointerEx(): Unit = {
    val s =  """
               |<caret>
               |
             """.stripMargin
    myFixture.configureByText(ComposerJson, s.replace("\r", ""))

    try {
      val element = myFixture.getElementAtCaret
      documentationProvider.getUrlFor(element, element)
    } catch {
      case ex: AssertionError if ex.getMessage.startsWith("element not found") =>
        // ignore - in this case from 2018.1 element at caret is not found, so exception is thrown
    }
  }
}
