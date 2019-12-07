package org.psliwa.idea.composerJson.intellij.codeAssist.php

import com.intellij.json.psi.{JsonProperty, JsonStringLiteral}
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.StandardPatterns._
import com.intellij.psi._
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson.intellij.PsiElements._
import org.psliwa.idea.composerJson.intellij.codeAssist.scripts.ScriptsPsiElementPattern

class PhpReferenceContributor extends PsiReferenceContributor {

  override def registerReferenceProviders(registrar: PsiReferenceRegistrar): Unit = {
    registerCallbackProvider(registrar)
    registerNamespaceProvider(registrar)
  }

  private def registerCallbackProvider(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(
      ScriptsPsiElementPattern.Pattern,
      PhpCallbackReferenceProvider
    )
  }

  private def registerNamespaceProvider(registrar: PsiReferenceRegistrar): Unit = {
    val rootElement = psiElement(classOf[JsonProperty])
      .withName("autoload", "autoload-dev")
      .withSuperParent(2, rootPsiElementPattern)

    registrar.registerReferenceProvider(
      psiElement(classOf[JsonStringLiteral])
        .and(
          or(
            psiElement().beforeLeaf(psiElement().withText(":")),
            //1 element is property name, second PsiErrorElement
            psiElement().withParent(psiElement().withChildren(collection().last(psiElement(classOf[PsiErrorElement]))))
          )
        )
        .withParent(classOf[JsonProperty])
        .withSuperParent(
          3,
          psiElement(classOf[JsonProperty])
            .withName("psr-0", "psr-4")
            .withSuperParent(2, rootElement)
        ),
      PhpNamespaceReferenceProvider
    )
  }
}

private object PhpCallbackReferenceProvider extends PsiReferenceProvider {
  override def getReferencesByElement(element: PsiElement, context: ProcessingContext): Array[PsiReference] = {
    val maybeReferences = for {
      stringElement <- ensureJsonStringLiteral(element)
    } yield {
      Array[PsiReference](new PhpCallbackReference(stringElement))
    }

    maybeReferences.getOrElse(Array())
  }
}

private object PhpNamespaceReferenceProvider extends PsiReferenceProvider {
  override def getReferencesByElement(element: PsiElement, context: ProcessingContext): Array[PsiReference] = {
    val maybeReferences = for {
      property <- ensureJsonStringLiteral(element)
    } yield {
      Array[PsiReference](new PhpNamespaceReference(property))
    }

    maybeReferences.getOrElse(Array())
  }
}
