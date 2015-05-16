package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.psliwa.idea.composerJson._
import org.junit.Assert._

abstract class DocumentationTest extends LightPlatformCodeInsightFixtureTestCase {
  protected def checkDocumentation(s: String, externalUrls: List[String], maybeExpectedDoc: Option[String] = None): Unit = {
    import scala.collection.JavaConverters._

    myFixture.configureByText(ComposerJson, s.replace("\r", ""))

    val element = myFixture.getElementAtCaret.getFirstChild.getFirstChild

    val urls = Option(documentationProvider.getUrlFor(element, element)).map(_.asScala).getOrElse(List())
    val doc = documentationProvider.generateDoc(element, element.getOriginalElement)

    externalUrls.foreach(url => assertTrue(urls.exists(_.contains(url))))
    maybeExpectedDoc.foreach(assertEquals(_, doc))
  }

  protected def checkDocumentation(s: String, expectedDoc: String): Unit = checkDocumentation(s, List(), Option(expectedDoc))

  protected def documentationProvider: DocumentationProvider
}
