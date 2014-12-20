package org.psliwa.idea.composer.idea.completionContributor

import com.intellij.codeInsight.completion._
import com.intellij.json.JsonLanguage
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.psliwa.idea.composer.idea.{Keyword, CompletionContributor}

abstract class TestCase extends LightPlatformCodeInsightFixtureTestCase {
  def getCompletionContributor = {
    getCompletionContributors.head
  }

  def getCompletionContributors = {
    import scala.collection.JavaConverters._

    CompletionContributor.forLanguage(JsonLanguage.INSTANCE).asScala
      .filter(_.isInstanceOf[CompletionContributor])
      .map(_.asInstanceOf[CompletionContributor])
  }

  def setCompletionPackageLoader(f: () => Seq[Keyword]) = {
    getCompletionContributors.foreach(_.setPackagesLoader(f))
  }

  def setCompletionVersionsLoader(f: String => Seq[String]) = {
    getCompletionContributors.foreach(_.setVersionsLoader(f))
  }

  protected def suggestions(contents: String, expectedSuggestions: Array[String], unexpectedSuggestions: Array[String] = Array()) = {
    myFixture.configureByText("composer.json", contents)
    myFixture.completeBasic()

    val lookupElements = myFixture.getLookupElementStrings

    assertContainsElements(lookupElements, expectedSuggestions:_*)
    assertDoesntContain(lookupElements, unexpectedSuggestions:_*)
  }

  protected def completion(contents: String, expected: String) = {
    myFixture.configureByText("composer.json", contents)
    myFixture.completeBasic()

    myFixture.checkResult(expected.replace("\r", ""))
  }
}
