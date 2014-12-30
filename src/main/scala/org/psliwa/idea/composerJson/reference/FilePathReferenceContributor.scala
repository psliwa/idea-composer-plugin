package org.psliwa.idea.composerJson.reference

import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.patterns.PlatformPatterns._
import com.intellij.psi._
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
      case SFilePath(_) => {
        List(ReferenceMatcher(psiElement(classOf[JsonStringLiteral]).withParent(parent), FilePathReferenceProvider))
      }
      case SFilePaths(_) => {
        val root = psiElement(classOf[JsonProperty]).withParent(psiElement(classOf[JsonObject]).withParent(parent))
        List(
          ReferenceMatcher(psiElement(classOf[JsonStringLiteral]).withParent(root).afterLeaf(":"), FilePathReferenceProvider),
          ReferenceMatcher(psiElement(classOf[JsonStringLiteral]).withParent(psiElement(classOf[JsonArray]).withParent(root)), FilePathReferenceProvider)
        )
      }
      case SPackages => {
        List(ReferenceMatcher(psiElement(classOf[JsonProperty]).withParent(psiElement(classOf[JsonObject]).withParent(parent)), PackageReferenceProvider))
      }
      case _ => Nil
    }

    loop(s, rootPsiElementPattern)
  }

  private def rootPsiElementPattern: PsiElementPattern.Capture[JsonFile] = {
    psiElement(classOf[JsonFile])
      .withLanguage(JsonLanguage.INSTANCE)
      .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
  }

  private case class ReferenceMatcher(pattern: ElementPattern[_ <: PsiElement], provider: PsiReferenceProvider)
}
