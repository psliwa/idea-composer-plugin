package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.psliwa.idea.composerJson._
import org.junit.Assert._

abstract class DocumentationTest extends LightPlatformCodeInsightFixtureTestCase {

  override def isWriteActionRequired: Boolean = true

  protected def checkDocumentation(s: String, externalUrls: List[String], maybeExpectedDoc: Option[String] = None, filename: String = ComposerJson): Unit = {
    def externalUrlsAssertion(urls: List[String]): Unit =  externalUrls.foreach(url => assertTrue(urls.exists(_.contains(url))))
    def docAssertion(doc: String): Unit = maybeExpectedDoc.foreach(assertEquals(_, doc))

    checkDocumentation(s, externalUrlsAssertion _, docAssertion _, filename)
  }

  protected def checkDocumentation(s: String, externalUrlsAssertion: List[String] => Unit, docAssertion: String => Unit, filename: String): Unit = {
    import scala.collection.JavaConverters._

    myFixture.configureByText(filename, s.replace("\r", ""))

    val element = myFixture.getElementAtCaret.getFirstChild.getFirstChild

    val urls = Option(documentationProvider.getUrlFor(element, element)).map(_.asScala).getOrElse(List())
    val doc = documentationProvider.generateDoc(element, element.getOriginalElement)

    externalUrlsAssertion(urls.toList)
    docAssertion(doc)
  }

  protected def checkDocumentation(s: String, expectedDoc: String): Unit = checkDocumentation(s, List(), Option(expectedDoc))

  protected def documentationProvider: DocumentationProvider
}
