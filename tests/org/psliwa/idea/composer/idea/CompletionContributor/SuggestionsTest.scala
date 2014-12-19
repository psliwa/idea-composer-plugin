package org.psliwa.idea.composer.idea.completionContributor

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

abstract class SuggestionsTest extends LightPlatformCodeInsightFixtureTestCase with TestCase {
  protected def suggestions(contents: String, expectedSuggestions: Array[String], unexpectedSuggestions: Array[String] = Array()) = {
    myFixture.configureByText("composer.json", contents)
    myFixture.completeBasic()

    val lookupElements = myFixture.getLookupElementStrings

    assertContainsElements(lookupElements, expectedSuggestions:_*)
    assertDoesntContain(lookupElements, unexpectedSuggestions:_*)
  }
}
