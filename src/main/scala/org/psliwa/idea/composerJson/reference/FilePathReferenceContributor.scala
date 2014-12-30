package org.psliwa.idea.composerJson.reference

import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.patterns.PlatformPatterns._
import com.intellij.psi._
import com.intellij.psi.impl.source.resolve.reference.impl.providers.{FileReferenceSet, FileReference}
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson._
import com.intellij.patterns.{ElementPattern, PsiElementPattern}
import org.psliwa.idea.composerJson.json._

class FilePathReferenceContributor extends PsiReferenceContributor  {
  val schema = ComposerSchema

  private type Capture = PsiElementPattern.Capture[_ <: PsiElement]

  override def registerReferenceProviders(registrar: PsiReferenceRegistrar): Unit = {
    schema
      .map(schemaToPatterns)
      .foreach(
        _.foreach { pattern => registrar.registerReferenceProvider(pattern, psiReferenceProvider) }
      )
  }

  private def schemaToPatterns(s: Schema): List[ElementPattern[_ <: PsiElement]] = {
    def loop(s: Schema, parent: Capture): List[ElementPattern[_ <: PsiElement]] = s match {
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
      case SFilePath(_) => {
        List(psiElement(classOf[JsonStringLiteral]).withParent(parent))
      }
      case SFilePaths(_) => {
        val root = psiElement(classOf[JsonProperty]).withParent(psiElement(classOf[JsonObject]).withParent(parent))
        List(
          psiElement(classOf[JsonStringLiteral]).withParent(root),
          psiElement(classOf[JsonStringLiteral]).withParent(psiElement(classOf[JsonArray]).withParent(root))
        )
      }
      case _ => Nil
    }

    loop(s, rootPsiElementPattern)
  }

  private val psiReferenceProvider = new PsiReferenceProvider {
    override def getReferencesByElement(element: PsiElement, context: ProcessingContext): Array[PsiReference] = {
      new FileReferenceSet(element).getAllReferences.asInstanceOf[Array[PsiReference]]
    }
  }

  private def rootPsiElementPattern: PsiElementPattern.Capture[JsonFile] = {
    psiElement(classOf[JsonFile])
      .withLanguage(JsonLanguage.INSTANCE)
      .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
  }
}
