package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion.{CompletionResultSet, CompletionParameters, CompletionProvider}
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.{JsonFile, JsonProperty}
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext
import ContextAwareCompletionProvider._

import scala.annotation.tailrec
import scala.collection.Seq

protected[idea] case class ContextAwareCompletionProvider(loadKeywords: Context => Seq[String]) extends CompletionProvider[CompletionParameters] with CompletionProviderMixin {
  override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
    val typedQuery = getTypedText(parameters.getPosition).getOrElse("")
    val keywords = firstNamedProperty(parameters.getPosition).map(p => Context(p.getName, typedQuery)).map(loadKeywords).getOrElse(List()).map(Keyword(_))

    addKeywordsToResult(keywords)(parameters, result)
  }

  private def getTypedText(e: PsiElement): Option[String] = e match {
    case LeafPsiElement(text) => Some(text).map(removeEmptyPalceholder)
    case _ => None
  }

  private def removeEmptyPalceholder(s: String) = s.replace(emptyNamePlaceholder+" ", "").replace(emptyNamePlaceholder, "")

  @tailrec
  private def firstNamedProperty(element: PsiElement): Option[JsonProperty] = {
    element match {
      case p@JsonProperty(name) => Some(p)
      case _: JsonFile => None
      case e => firstNamedProperty(e.getParent)
    }
  }

  private object JsonProperty {
    def unapply(x: JsonProperty): Option[(String)] = if(x.getName.contains(emptyNamePlaceholder)) None else Some(x.getName)
  }

  private object LeafPsiElement {
    def unapply(x: LeafPsiElement): Option[(String)] = Some(x.getText)
  }


}

protected[idea] object ContextAwareCompletionProvider {
  case class Context(propertyName: String, typedQuery: String)
}

protected[idea] case class KeywordsCompletionProvider(keywords: Keywords, getInsertHandler: InsertHandlerFinder = _ => None)
  extends CompletionProvider[CompletionParameters] with CompletionProviderMixin {

  override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
    addKeywordsToResult(keywords(), getInsertHandler)(parameters, result)
  }
}

protected[idea] trait CompletionProviderMixin {
  protected def addKeywordsToResult(keywords: Iterable[Keyword], getInsertHandler: InsertHandlerFinder = _ => None)
      (parameters: CompletionParameters, result: CompletionResultSet) {

    keywords.foreach(k => {
      result.addElement(LookupElementBuilder.create(k.text).withInsertHandler(insertHandler(parameters.getPosition, k, getInsertHandler)))
    })
  }

  protected def insertHandler(element: PsiElement, keyword: Keyword, getInsertHandler: InsertHandlerFinder) = {
    if(!keyword.quoted) null
    else getInsertHandler(keyword).getOrElse(QuoteInsertHandler)
  }
}