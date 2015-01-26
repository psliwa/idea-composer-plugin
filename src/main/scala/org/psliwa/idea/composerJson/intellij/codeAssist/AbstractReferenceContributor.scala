package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.json.psi.{JsonArray, JsonObject, JsonProperty}
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns._
import com.intellij.psi.{PsiElement, PsiReferenceContributor, PsiReferenceProvider, PsiReferenceRegistrar}
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.intellij.PsiElements
import org.psliwa.idea.composerJson.json.{SArray, SObject, Schema}
import PsiElements.rootPsiElementPattern

abstract class AbstractReferenceContributor extends PsiReferenceContributor {
  private val schema = ComposerSchema

  override final def registerReferenceProviders(registrar: PsiReferenceRegistrar): Unit = {
    schema
      .map(schemaToPatterns)
      .foreach(
        _.foreach { matcher => registrar.registerReferenceProvider(matcher.pattern, matcher.provider) }
      )
  }

  private def schemaToPatterns(s: Schema): List[ReferenceMatcher] = {
    def loop(s: Schema, parent: Capture): List[ReferenceMatcher] = s match {
      case SObject(properties, _) => {
        properties.toList.flatMap{ case(name, property) => {
          loop(
            property.schema,
            psiElement(classOf[JsonProperty]).withName(name).withParent(psiElement(classOf[JsonObject]).withParent(parent))
          )
        }}
      }
      case SArray(item) => {
        loop(item, psiElement(classOf[JsonArray]).withParent(parent))
      }
      case _ => schemaToPatterns(s, parent)
    }

    loop(s, rootPsiElementPattern)
  }

  protected def schemaToPatterns(s: Schema, parent: Capture): List[ReferenceMatcher]

  protected class ReferenceMatcher(val pattern: ElementPattern[_ <: PsiElement], val provider: PsiReferenceProvider)
}
