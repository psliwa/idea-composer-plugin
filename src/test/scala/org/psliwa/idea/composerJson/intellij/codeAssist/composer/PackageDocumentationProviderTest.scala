package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.lang.documentation.DocumentationProvider
import org.psliwa.idea.composerJson.composer.ComposerPackage
import org.psliwa.idea.composerJson.intellij.codeAssist.DocumentationTest
import org.psliwa.idea.composerJson.fixtures.ComposerFixtures._
import org.psliwa.idea.composerJson.composer.ComposerPackages

class PackageDocumentationProviderTest extends DocumentationTest {
  override protected def documentationProvider: DocumentationProvider = new PackageDocumentationProvider

  def testGivenPackage_thereShouldBeUrlToPackagistAsExternalDocumentation() = {
    checkDocumentation(
      """
        |{
        |  "require": {
        |    "vendor<caret>/pkg": "1.0.0"
        |  }
        |}
      """.stripMargin,
      List("packagist.org/packages/vendor/pkg")
    )
  }

  def testGivenPackage_homepageExistsInComposerLock_theUrlShouldBeTheSameAsHomepage() = {
    createComposerLock(myFixture, ComposerPackages(ComposerPackage("vendor/pkg", "1.0.0", homepage = Some("some/url"))))

    checkDocumentation(
      """
        |{
        |  "require": {
        |    "vendor<caret>/pkg": "1.0.0"
        |  }
        |}
      """.stripMargin,
      List("some/url")
    )
  }
}
