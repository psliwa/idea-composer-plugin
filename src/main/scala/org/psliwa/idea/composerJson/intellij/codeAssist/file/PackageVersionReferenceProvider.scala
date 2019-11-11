package org.psliwa.idea.composerJson.intellij.codeAssist.file

import com.intellij.psi.{PsiElement, PsiReference, PsiReferenceProvider}
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson.composer.model.PackageDescriptor._
import org.psliwa.idea.composerJson.composer.model.PackageName
import org.psliwa.idea.composerJson.intellij.PsiElements._
import org.psliwa.idea.composerJson.util.ImplicitConversions._

private object PackageVersionReferenceProvider extends PsiReferenceProvider {
  private val EmptyReferences: Array[PsiReference] = Array()

  override def getReferencesByElement(element: PsiElement, context: ProcessingContext): Array[PsiReference] = {
    val maybeReferences = for {
      propertyValue <- ensureJsonStringLiteral(element)
      property <- ensureJsonProperty(element.getParent)
      propertyName <- ensureJsonStringLiteral(property.getNameElement)
      packageName = PackageName(propertyName.getValue.stripQuotes)
    } yield Array[PsiReference](new UrlPsiReference(propertyValue) {
      override protected def url: Option[String] = documentationUrl(element, packageName)
    })

    maybeReferences.getOrElse(EmptyReferences)
  }
}
