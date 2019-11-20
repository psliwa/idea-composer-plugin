package org.psliwa.idea.composerJson.intellij.codeAssist.php

import com.intellij.codeInsight.completion.impl.CamelHumpMatcher
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.project.Project
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.{PsiElementResolveResult, PsiPolyVariantReferenceBase, ResolveResult}
import com.jetbrains.php.completion.PhpLookupElement
import com.jetbrains.php.{PhpIcons, PhpIndex}
import org.psliwa.idea.composerJson.intellij.codeAssist.php.PhpNamespaceReference._
import org.psliwa.idea.composerJson.intellij.codeAssist.php.PhpUtils._

import scala.jdk.CollectionConverters._

private class PhpNamespaceReference(element: JsonStringLiteral) extends PsiPolyVariantReferenceBase[JsonStringLiteral](element) {
  private val namespaceName = getFixedReferenceName(element.getText)

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val phpIndex = PhpIndex.getInstance(element.getProject)

    phpIndex.getNamespacesByName("\\"+namespaceName.stripSuffix("\\")).asScala
      .map(new PsiElementResolveResult(_))
      .toArray
  }

  override def getVariants: Array[AnyRef] = {
    import org.psliwa.idea.composerJson.util.CharOffsetFinder._
    import org.psliwa.idea.composerJson.util.OffsetFinder.ImplicitConversions._

    val o = for {
      lastSlashOffset <- findOffsetReverse('\\')(namespaceName.length-1)(namespaceName)
    } yield (namespaceName.substring(0, lastSlashOffset), namespaceName.substring(lastSlashOffset+1))

    val (parentNamespace, currentNamespace) = o match {
      case Some(x) => x
      case None => "" -> ""
    }

    val phpIndex = PhpIndex.getInstance(element.getProject)

    val methodMatcher = new CamelHumpMatcher(currentNamespace)

    phpIndex.getChildNamespacesByParentName(ensureLandingSlash(parentNamespace+"\\")).asScala
      .filter(methodMatcher.prefixMatches)
      .map(namespace => new PhpNamespaceLookupElement(element.getProject, (parentNamespace+"\\"+namespace+"\\").stripPrefix("\\")))
      .toArray
  }
}

private object PhpNamespaceReference {
  lazy val NamespaceStubIndexKey = StubIndexKey.createIndexKey("org.psliwa.idea.composerJson.phpNamespace")

  private class PhpNamespaceLookupElement(project: Project, namespace: String) extends PhpLookupElement(
    escapeSlashes(namespace), NamespaceStubIndexKey, PhpIcons.NAMESPACE, null, project, null
  ) {
    override def renderElement(presentation: LookupElementPresentation): Unit = {
      super.renderElement(presentation)
      presentation.setItemText(namespace)
    }
  }
}
