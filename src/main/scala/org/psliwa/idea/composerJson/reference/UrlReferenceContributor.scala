package org.psliwa.idea.composerJson.reference

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns._
import org.psliwa.idea.composerJson.json._

class UrlReferenceContributor extends AbstractReferenceContributor {
  override protected def schemaToPatterns(schema: Schema, parent: Capture): List[ReferenceMatcher] = schema match {
    case SString(UriFormat | EmailFormat) => List(ReferenceMatcher(psiElement(classOf[JsonStringLiteral]).withParent(parent), UrlReferenceProvider))
    case _ => Nil
  }
}
