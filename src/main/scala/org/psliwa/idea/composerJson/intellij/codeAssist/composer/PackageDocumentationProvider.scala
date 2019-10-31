package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import java.util

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.{PsiManager, PsiElement}
import com.intellij.patterns.PlatformPatterns._
import org.psliwa.idea.composerJson.intellij.PsiElements._
import org.psliwa.idea.composerJson.composer.PackageDescriptor._

class PackageDocumentationProvider extends DocumentationProvider {
  import PackageDocumentationProvider._

  override def getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement): String = null

  override def getDocumentationElementForLookupItem(psiManager: PsiManager, `object`: scala.Any,
    element: PsiElement): PsiElement = null

  override def getDocumentationElementForLink(psiManager: PsiManager, link: String,
    context: PsiElement): PsiElement = null

  override def getUrlFor(element: PsiElement, originalElement: PsiElement): util.List[String] = {
    if(packageNamePattern.accepts(originalElement)) {
      util.Arrays.asList(documentationUrl(originalElement, getStringValue(originalElement.getParent).getOrElse("")))
    } else {
      null
    }
  }

  override def generateDoc(element: PsiElement, originalElement: PsiElement): String = null
}

private object PackageDocumentationProvider {
  val packageNamePattern = psiElement().withParent(
    packageElement.beforeLeaf(psiElement().withText(":"))
  )
}
