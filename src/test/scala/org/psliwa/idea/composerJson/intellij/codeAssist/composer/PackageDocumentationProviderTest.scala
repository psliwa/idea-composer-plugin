package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.lang.documentation.DocumentationProvider
import org.psliwa.idea.composerJson.intellij.codeAssist.DocumentationTest

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
}
