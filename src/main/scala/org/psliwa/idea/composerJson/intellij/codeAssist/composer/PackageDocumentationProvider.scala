package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import java.util

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.{PsiElement, PsiManager}
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.PsiElementPattern
import org.psliwa.idea.composerJson.intellij.PsiElements._
import org.psliwa.idea.composerJson.composer.model.PackageDescriptor._
import org.psliwa.idea.composerJson.composer.model.PackageName

class PackageDocumentationProvider extends DocumentationProvider {
  import PackageDocumentationProvider._

  override def getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement): String = null

  override def getDocumentationElementForLookupItem(psiManager: PsiManager,
                                                    `object`: scala.Any,
                                                    element: PsiElement): PsiElement = null

  override def getDocumentationElementForLink(psiManager: PsiManager, link: String, context: PsiElement): PsiElement =
    null

  override def getUrlFor(element: PsiElement, originalElement: PsiElement): util.List[String] = {
    if (packageNamePattern.accepts(originalElement)) {
      import scala.jdk.CollectionConverters._
      documentationUrl(originalElement, PackageName(getStringValue(originalElement.getParent).getOrElse(""))).toList.asJava
    } else {
      null
    }
  }

  override def generateDoc(element: PsiElement, originalElement: PsiElement): String = null
}

private object PackageDocumentationProvider {
  val packageNamePattern: PsiElementPattern.Capture[PsiElement] = psiElement().withParent(
    packageElement.beforeLeaf(psiElement().withText(":"))
  )
}
