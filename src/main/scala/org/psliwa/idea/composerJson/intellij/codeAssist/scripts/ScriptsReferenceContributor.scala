package org.psliwa.idea.composerJson.intellij.codeAssist.scripts

import com.intellij.json.psi.JsonObject
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
    def findScriptsHolder(root: JsonObject): Option[JsonObject] = {
      Option(root.findProperty("scripts")).flatMap(a => Option(a.getValue)).flatMap(ensureJsonObject)
    }

    val maybeReferences = for {
      stringElement <- ensureJsonStringLiteral(element)
    } yield {
      Array[PsiReference](
        new ScriptsReference(stringElement),
        new ScriptAliasReference(findScriptsHolder, stringElement)
      )
    }

    maybeReferences.getOrElse(Array())
  }
}
