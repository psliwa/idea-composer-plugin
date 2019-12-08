package org.psliwa.idea.composerJson.intellij.codeAssist.scripts

import java.util.Collections

import com.intellij.codeInsight.completion.FilePathCompletionContributor.FilePathLookupItem
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.util.TextRange
import com.intellij.psi.{PsiDirectory, PsiElementResolveResult, PsiPolyVariantReferenceBase, ResolveResult}
import org.psliwa.idea.composerJson.intellij.codeAssist.References

private class ScriptsReference(element: JsonStringLiteral)
    extends PsiPolyVariantReferenceBase[JsonStringLiteral](element) {
  private val referenceName: String =
    References.getFixedReferenceName(element.getText).split(' ').headOption.getOrElse("")

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val maybeCommandFile = for {
      binDir <- maybeBinDir
      commandFile <- Option(binDir.findFile(referenceName))
    } yield commandFile

    maybeCommandFile.map(new PsiElementResolveResult(_)).toArray
  }

  private def maybeBinDir: Option[PsiDirectory] = {
    for {
      rootDir <- Option(element.getContainingFile.getOriginalFile.getContainingDirectory)
      vendorDir <- Option(rootDir.findSubdirectory("vendor"))
      binDir <- Option(vendorDir.findSubdirectory("bin"))
    } yield binDir
  }

  override def getRangeInElement: TextRange = {
    val textRange = super.getRangeInElement
    new TextRange(textRange.getStartOffset, textRange.getStartOffset + referenceName.length)
  }

  override def getVariants: Array[AnyRef] = {
    maybeBinDir match {
      case Some(binDir) =>
        binDir.getFiles.map(new FilePathLookupItem(_, Collections.emptyList()))
      case None =>
        Array()
    }
  }
}
