package org.psliwa.idea.composer.idea

import com.intellij.json.JsonElementTypes
import com.intellij.json.psi.{JsonProperty, JsonElement}
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.IElementType
import org.psliwa.idea.composer.parser._

import scala.annotation.tailrec

protected object Completion {

  private lazy val allowedPreviousChars = List(',', '{', '[', ':')

  def getSchemaFor(s: Schema)(e: PsiElement): Option[Schema] = {

    val l = getJsonPath(e)

    @tailrec
    def loop(s: Option[Schema], l: List[PsiElement]): Option[Schema] = {
      (s, l) match {
        case (None, _) => None
        case (_, Nil) => s
        case (Some(SObject(props1)), JsonProperty(name)::t) if name != "" => loop(props1.get(name), t)
        case (s@Some(_), JsonElement(_)::t)  => loop(s, t)
        case (_, _) => s
      }
    }

    loop(Some(s), l)
  }

  def getCompletionsFor(s: Schema)(e: PsiElement): List[String] = {
    getSchemaFor(s)(e)
      .map(getCompletionsForSchema)
      .getOrElse(List())
  }

  def getCompletionsForSchema(s: Schema): List[String] = s match {
    case SObject(m) => m.keys.toList
    case SStringChoice(m) => m
    case SOr(l) => l.flatMap(getCompletionsForSchema)
      //TODO: SArray
    case SBoolean => List("true", "false")
    case _ => List()
  }

  private def getJsonPath(e: PsiElement): List[JsonElement] = {

    @tailrec
    def loop(e: PsiElement, l: List[JsonElement]): List[JsonElement] = {
      e match {
        case j@JsonElement(parent) => loop(parent, l :+ j)
        case PsiElementWithJsonPropertyAsPrevSibling(jsonProperty) => loop(e.getParent, l :+ jsonProperty)
        case ColonElementWithPrevSibling(prevSibling) => loop(prevSibling, l)
        case PsiElementWithOpenTokenPrevSibling(parent) => loop(parent, l)
        case LeafPsiElement(JsonElementTypes.DOUBLE_QUOTED_STRING, text) => loop(e.getParent, l)
        case _ => l
      }
    }

    loop(e, List()).reverse
  }

  private object JsonElement {
    def unapply(x: JsonElement): Option[(PsiElement)] = Some(x.getParent)
  }
  private object LeafPsiElement {
    def unapply(x: LeafPsiElement): Option[(IElementType, String)] = Some((x.getElementType, x.getText))
  }

  private object PsiElementWithJsonPropertyAsPrevSibling {
    def unapply(x: PsiElement): Option[(JsonProperty)] = Option(x.getPrevSibling).filter(_.isInstanceOf[JsonProperty]).map(_.asInstanceOf[JsonProperty])
  }

  private object ColonElementWithPrevSibling {
    def unapply(x: PsiElement): Option[(PsiElement)] = if(x.getText == "," && x.getPrevSibling != null) Option(x.getPrevSibling) else None
  }

  private object PsiElementWithOpenTokenPrevSibling {
    def unapply(x: PsiElement): Option[(PsiElement)] = {
      if(Option(x.getPrevSibling).exists(_.getText.lastOption.exists(c => allowedPreviousChars.contains(c)))) {
        Option(x.getParent)
      } else {
        None
      }
    }
  }

  private object JsonProperty {
    def unapply(x: JsonProperty): Option[(String)] = Option(x.getName)
  }
}
