package org.psliwa.idea.composerJson.intellij.codeAssist.scripts

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.json.psi.{JsonElement, JsonObject, JsonProperty, JsonStringLiteral}
import com.intellij.psi.{PsiElementResolveResult, PsiPolyVariantReferenceBase, ResolveResult}
import org.psliwa.idea.composerJson.intellij.PsiElements
import org.psliwa.idea.composerJson.intellij.codeAssist.scripts.ScriptAliasReference.ScriptAliasLookupElement
import org.psliwa.idea.composerJson.util.ImplicitConversions._

import scala.jdk.CollectionConverters._

class ScriptAliasReference(findScriptsHolder: JsonObject => Option[JsonObject], element: JsonStringLiteral)
    extends PsiPolyVariantReferenceBase[JsonStringLiteral](element) {
  override def multiResolve(b: Boolean): Array[ResolveResult] = {
    val currentElementScript = element.getText.stripQuotes.stripPrefix("@")
    getScriptProperties()
      .map(_.getNameElement)
      .filter(_.getText.stripQuotes == currentElementScript)
      .map(new PsiElementResolveResult(_))
      .toArray
  }

  override def getVariants: Array[AnyRef] = {
    val currentScriptName = PsiElements.findParentProperty(element).map(_.getName)

    getScriptProperties()
      .filterNot(property => currentScriptName.contains(property.getName))
      .flatMap(property => Option(property.getNameElement))
      .map(new ScriptAliasLookupElement(_))
      .toArray ++ Array("@composer", "@php")
  }

  private def getScriptProperties(): List[JsonProperty] = {
    for {
      root <- element.getContainingFile.getChildren.flatMap(PsiElements.ensureJsonObject).headOption.toList
      scriptsHolder <- findScriptsHolder(root).toList
      property <- scriptsHolder.getPropertyList.asScala.toList
    } yield property
  }
}

private object ScriptAliasReference {
  class ScriptAliasLookupElement(element: JsonElement) extends LookupElement {
    override def getLookupString: String = "@" + element.getText.stripQuotes
    override def getObject: AnyRef = element
  }
}
