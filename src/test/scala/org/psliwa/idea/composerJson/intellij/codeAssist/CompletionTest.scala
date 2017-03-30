package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.junit.Assert._
import org.psliwa.idea.composerJson.ComposerJson

abstract class CompletionTest extends LightPlatformCodeInsightFixtureTestCase {

  override def isWriteActionRequired: Boolean = false

  protected def suggestions(
    contents: String,
    expectedSuggestions: Array[String],
    unexpectedSuggestions: Array[String] = Array()
  ): Unit = suggestions(UsefulTestCase.assertContainsElements(_, _:_*))(contents, expectedSuggestions, unexpectedSuggestions)

  protected def orderedSuggestions(
    contents: String,
    expectedSuggestions: Array[String],
    unexpectedSuggestions: Array[String] = Array()
  ): Unit = suggestions(UsefulTestCase.assertContainsOrdered(_, _:_*))(contents, expectedSuggestions, unexpectedSuggestions)

  protected def suggestions(
    containsElements: (java.util.List[String], Array[String]) => Unit
  )(
    contents: String, 
    expectedSuggestions: Array[String],
    unexpectedSuggestions: Array[String]
  ) = {
    myFixture.configureByText(ComposerJson, contents)
    myFixture.completeBasic()

    val lookupElements = myFixture.getLookupElementStrings

    assertNotNull(lookupElements)
    containsElements(lookupElements, expectedSuggestions)
    UsefulTestCase.assertDoesntContain(lookupElements, unexpectedSuggestions:_*)
  }

  protected def completion(contents: String, expected: String) = {
    myFixture.configureByText(ComposerJson, contents)
    val elements = myFixture.completeBasic()

    if(elements != null && elements.length == 1) {
      //finish completion if there is only one item
      myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
    }

    myFixture.checkResult(expected.replace("\r", ""))
  }

  def writeAction(f: () => Unit): Unit = {
    ApplicationManager.getApplication.runWriteAction(new Computable[Unit] {
      override def compute = f()
    })
  }
}
