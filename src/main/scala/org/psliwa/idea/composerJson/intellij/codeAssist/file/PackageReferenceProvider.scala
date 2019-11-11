package org.psliwa.idea.composerJson.intellij.codeAssist.file

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.impl.source.resolve.reference.impl.providers.{FileReference, FileReferenceSet}
import com.intellij.psi.{ElementManipulators, PsiElement, PsiReference, PsiReferenceProvider}
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson
import org.psliwa.idea.composerJson.composer.model.PackageName
import org.psliwa.idea.composerJson.intellij.PsiElements._

private object PackageReferenceProvider extends PsiReferenceProvider {
  private val EmptyReferences: Array[PsiReference] = Array()

  override def getReferencesByElement(element: PsiElement, context: ProcessingContext): Array[PsiReference] = {
    val maybeReferences = for {
      name <- ensureJsonStringLiteral(element)
      references <- nameToReferences(name)
    } yield references

    maybeReferences.getOrElse(EmptyReferences)
  }

  private def nameToReferences(nameElement: JsonStringLiteral): Option[Array[PsiReference]] = {
    val range = ElementManipulators.getValueTextRange(nameElement)
    val packageName = PackageName(range.substring(nameElement.getText))

    packageName.`vendor/project`
      .map {
        case(vendor, project) if !project.contains(composerJson.EmptyPsiElementNamePlaceholder) => {
          val set = new FileReferenceSet(s"vendor/$vendor/$project", nameElement, range.getStartOffset, this, true)
          Array[PsiReference](
            new FileReference(set, new TextRange(1, vendor.length + 1), 0, s"vendor/$vendor"),
            new FileReference(set, new TextRange(1, vendor.length+project.length+2), 0, s"vendor/$vendor/$project")
          )
        }
        case _ => Array()
      }
  }
}
