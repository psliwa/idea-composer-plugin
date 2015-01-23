package org.psliwa.idea.composerJson.reference

import com.intellij.json.psi._
import com.intellij.patterns.PlatformPatterns._
import org.psliwa.idea.composerJson.json._

class FilePathReferenceContributor extends AbstractReferenceContributor  {

  override protected def schemaToPatterns(schema: Schema, parent: Capture): List[ReferenceMatcher] = schema match {
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
}
