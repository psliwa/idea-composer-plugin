package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.junit.Assert._
import org.psliwa.idea.composerJson.ComposerJson

abstract class CompletionTest extends LightPlatformCodeInsightFixtureTestCase {

  protected def suggestions(contents: String, expectedSuggestions: Array[String], unexpectedSuggestions: Array[String] = Array()) = {
    myFixture.configureByText(ComposerJson, contents)
    myFixture.completeBasic()

    val lookupElements = myFixture.getLookupElementStrings

    assertNotNull(lookupElements)
    assertContainsElements(lookupElements, expectedSuggestions:_*)
    assertDoesntContain(lookupElements, unexpectedSuggestions:_*)
  }

  protected def completion(contents: String, expected: String) = {
    myFixture.configureByText(ComposerJson, contents)
    myFixture.completeBasic()

    myFixture.checkResult(expected.replace("\r", ""))
  }
}
