package org.psliwa.idea.composerJson.intellij.codeAssist.file

import com.intellij.json.psi.JsonStringLiteral
import com.intellij.patterns.PlatformPatterns._
import org.psliwa.idea.composerJson.intellij.codeAssist.AbstractReferenceContributor
import org.psliwa.idea.composerJson.json._
import org.psliwa.idea.composerJson.intellij.codeAssist._

class UrlReferenceContributor extends AbstractReferenceContributor {
  override protected def schemaToPatterns(schema: Schema, parent: Capture): List[ReferenceMatcher] = schema match {
    case SString(UriFormat | EmailFormat) =>
      List(new ReferenceMatcher(psiElement(classOf[JsonStringLiteral]).withParent(parent), UrlReferenceProvider))
    case _ => Nil
  }
}
