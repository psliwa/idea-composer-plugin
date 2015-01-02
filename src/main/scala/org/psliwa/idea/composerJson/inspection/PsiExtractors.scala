package org.psliwa.idea.composerJson.inspection

import com.intellij.json.psi._
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement

private[inspection] object PsiExtractors {
  object JsonFile {
    def unapply(x: JsonFile): Option[(JsonValue)] = Option(x.getTopLevelValue)
  }

  object JsonObject {
    def unapply(x: JsonObject): Option[(java.util.List[JsonProperty])] = Some(x.getPropertyList)
  }

  object JsonProperty {
    def unapply(x: JsonProperty): Option[(String, JsonValue)] = Some((x.getName, x.getValue))
  }

  object JsonArray {
    def unapply(x: JsonArray): Option[(java.util.List[JsonValue])] = Some(x.getValueList)
  }

  object JsonValue {
    def unapply(x: JsonValue): Option[(String)] = Option(x.getText)
  }

  object JsonStringLiteral {
    import scala.collection.JavaConversions._
    def unapply(x: JsonStringLiteral): Option[(String)] = x.getTextFragments.headOption.map(_.second).orElse(Some(""))
  }

  object JsonBooleanLiteral {
    def unapply(x: JsonBooleanLiteral): Option[(Boolean)] = Some(x.getText.toBoolean)
  }

  object JsonNumberLiteral {
    def unapply(x: JsonNumberLiteral): Option[(Unit)] = Some(())
  }

  object LeafPsiElement {
    def unapply(x: LeafPsiElement): Option[(String)] = Some(x.getText)
  }

  object PsiWhiteSpace {
    def unapply(x: PsiWhiteSpace): Option[(Unit)] = Some(())
  }
}
