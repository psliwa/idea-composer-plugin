package org.psliwa.idea.composerJson.intellij.codeAssist.scripts

import org.junit.Assert
import org.psliwa.idea.composerJson.ComposerJson
import org.psliwa.idea.composerJson.intellij.codeAssist.CompletionTest
import org.psliwa.idea.composerJson.util.ImplicitConversions._

class ScriptAliasReferenceTest extends CompletionTest {

  def testScriptAliasesSuggestions_thereAreFewScripts_suggestThem(): Unit = {
    suggestions(
      """
        |{
        |  "scripts": {
        |    "script1": "",
        |    "script2": "",
        |    "script3": "<caret>"
        |  }
        |}
        |""".stripMargin,
      Array("@script1", "@script2")
    )
  }

  def testScriptAliasesSuggestions_suggestComposerAndPhpSpecialScripts(): Unit = {
    suggestions(
      """
        |{
        |  "scripts": {
        |    "script1": "<caret>"
        |  }
        |}
        |""".stripMargin,
      Array("@composer", "@php")
    )
  }

  def testScriptAliasesSuggestions_skipScriptWhereIsCaret(): Unit = {
    suggestions(
      """
        |{
        |  "scripts": {
        |    "script1": "",
        |    "script2": "",
        |    "script3": "<caret>"
        |  }
        |}
        |""".stripMargin,
      Array(),
      Array("@script3")
    )
  }

  def testScriptAliasesReferences_referenceExists(): Unit = {
    val references = getResolvedFileReferences(
      """
        |{
        |  "scripts": {
        |    "script1": "",
        |    "script3": "@script<caret>1"
        |  }
        |}
        |""".stripMargin
    )

    Assert.assertEquals(List("script1"), references)
  }

  private def getResolvedFileReferences(content: String): List[String] = {
    myFixture.configureByText(ComposerJson, content)

    val element = myFixture.getFile.findElementAt(myFixture.getCaretOffset).getParent

    element.getReferences
      .collect { case reference: ScriptAliasReference => reference }
      .flatMap(_.multiResolve(false))
      .map(_.getElement.getText)
      .map(_.stripQuotes)
      .toList
  }
}
