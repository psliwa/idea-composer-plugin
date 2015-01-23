package org.psliwa.idea.composerJson.reference

import com.intellij.json.JsonLanguage
import com.intellij.json.psi.{JsonArray, JsonObject, JsonProperty, JsonFile}
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.{ElementPattern, PsiElementPattern}
import com.intellij.psi.{PsiReferenceProvider, PsiReferenceRegistrar, PsiElement, PsiReferenceContributor}
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.json.{SArray, SObject, Schema}

abstract class AbstractReferenceContributor extends PsiReferenceContributor {
  private[reference] val schema = ComposerSchema

  private[reference] type Capture = PsiElementPattern.Capture[_ <: PsiElement]

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

  private def rootPsiElementPattern: PsiElementPattern.Capture[JsonFile] = {
    psiElement(classOf[JsonFile])
      .withLanguage(JsonLanguage.INSTANCE)
      .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
  }

  protected case class ReferenceMatcher(pattern: ElementPattern[_ <: PsiElement], provider: PsiReferenceProvider)
}
