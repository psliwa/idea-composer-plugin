package org.psliwa.idea.composerJson.intellij.codeAssist.file

import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.util.TextRange
import com.intellij.psi.impl.source.resolve.reference.impl.providers.{FileReference, FileReferenceSet}
import com.intellij.psi.{ElementManipulators, PsiElement, PsiReference, PsiReferenceProvider}
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson.intellij.PsiElements
import PsiElements._
import org.psliwa.idea.composerJson
import org.psliwa.idea.composerJson.composer.ComposerPackage._

private object PackageReferenceProvider extends PsiReferenceProvider {
  private val EmptyReferences: Array[PsiReference] = Array()

  override def getReferencesByElement(element: PsiElement, context: ProcessingContext): Array[PsiReference] = {
    val maybeReferences = for {
      property <- ensureJsonProperty(element)
      references <- propertyToReferences(property)
    } yield references

    maybeReferences.getOrElse(EmptyReferences)
  }

  private def propertyToReferences(property: JsonProperty): Option[Array[PsiReference]] = {
    val nameElement = property.getNameElement
    val range = ElementManipulators.getValueTextRange(nameElement)
    val text = range.substring(nameElement.getText)

    `vendor/package`(text)
      .map{
        case(vendor, pkg) if !pkg.contains(composerJson.EmptyPsiElementNamePlaceholder) => {
          val set = new FileReferenceSet("vendor/"+text, property, range.getStartOffset, this, true)
          Array[PsiReference](
            new FileReference(set, new TextRange(1, vendor.length + 1), 0, "vendor/"+vendor),
            new FileReference(set, new TextRange(1, vendor.length+pkg.length+2), 0, "vendor/"+vendor+"/"+pkg)
          )
        }
        case _ => Array()
      }
  }
}
