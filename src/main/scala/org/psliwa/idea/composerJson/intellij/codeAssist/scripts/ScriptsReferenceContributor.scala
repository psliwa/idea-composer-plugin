package org.psliwa.idea.composerJson.intellij.codeAssist.scripts

import com.intellij.psi._
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson.intellij.PsiElements._

class ScriptsReferenceContributor extends PsiReferenceContributor {
  override def registerReferenceProviders(registrar: PsiReferenceRegistrar): Unit = {
    registrar.registerReferenceProvider(
      ScriptsPsiElementPattern.Pattern,
      ScriptsReferenceProvider
    )
  }
}

private object ScriptsReferenceProvider extends PsiReferenceProvider {
  override def getReferencesByElement(element: PsiElement, context: ProcessingContext): Array[PsiReference] = {
    val maybeReferences = for {
      stringElement <- ensureJsonStringLiteral(element)
    } yield {
      Array[PsiReference](
        new ScriptsReference(stringElement)
      )
    }

    maybeReferences.getOrElse(Array())
  }
}
