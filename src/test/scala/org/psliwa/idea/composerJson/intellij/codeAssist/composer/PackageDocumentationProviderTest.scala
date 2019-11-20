package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.openapi.vfs.VirtualFile
import org.psliwa.idea.composerJson.composer.model.PackageDescriptor
import org.psliwa.idea.composerJson.fixtures.ComposerFixtures
import org.psliwa.idea.composerJson.fixtures.ComposerFixtures._
import org.psliwa.idea.composerJson.intellij.codeAssist.DocumentationTest

class PackageDocumentationProviderTest extends DocumentationTest {
  override protected def documentationProvider: DocumentationProvider = new PackageDocumentationProvider

  def testGivenPackage_thereShouldBeUrlToPackagistAsExternalDocumentation(): Unit = {
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

  def testGivenPackage_homepageExistsInComposerLock_theUrlShouldBeTheSameAsHomepage(): Unit = {
    createComposerLock(List(PackageDescriptor("vendor/pkg", "1.0.0", homepage = Some("some/url"))))

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

  private def createComposerLock(packages: List[PackageDescriptor]): VirtualFile = {
    ComposerFixtures.createComposerLock(myFixture, packages.map(ComposerPackageWithReplaces(_, Set.empty)))
  }
}
