package org.psliwa.idea.composerJson.intellij.codeAssist.scripts

import org.junit.Assert.assertEquals
import org.psliwa.idea.composerJson.intellij.codeAssist.FilePathReferences

class ScriptsReferenceTest extends FilePathReferences {

  def testScriptReference_givenExistingScript_referenceShouldExist(): Unit = {
    createScriptFile("superScript")

    val references = getResolvedFileReferences(
      _.contains("vendor"),
      """
        |{
        |  "scripts": {
        |    "custom": "superScript<caret>"
        |  }
        |}
      """.stripMargin
    )

    assertEquals(1, references.length)
  }

  def testScriptReference_givenExistingScriptWithParameters_referenceShouldExist(): Unit = {
    createScriptFile("superScript")

    val references = getResolvedFileReferences(
      _.contains("vendor"),
      """
        |{
        |  "scripts": {
        |    "custom": "super<caret>Script --some-param"
        |  }
        |}
      """.stripMargin
    )

    assertEquals(1, references.length)
  }

  def testScriptReference_givenUnexistingScript_referenceShouldNotExist(): Unit = {
    createScriptFile("superScript")

    val references = getResolvedFileReferences(
      _.contains("vendor"),
      """
        |{
        |  "scripts": {
        |    "custom": "anotherScript<caret>"
        |  }
        |}
      """.stripMargin
    )

    assertEquals(0, references.length)
  }

  def testScriptReference_givenExistingScript_commandShouldBeCompleted(): Unit = {
    createScriptFile("superScript")

    completion(
      """
        |{
        |  "scripts": {
        |    "custom": "sup<caret>"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "scripts": {
        |    "custom": "superScript<caret>"
        |  }
        |}
      """.stripMargin
    )
  }

  private def createScriptFile(scriptName: String): Unit = {
    writeAction(() => {
      myFixture.getTempDirFixture
        .findOrCreateDir("vendor")
        .createChildDirectory(this, "bin")
        .findOrCreateChildData(this, scriptName)
    })
  }
}
