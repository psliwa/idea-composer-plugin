package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion.{CompletionResultSet, CompletionParameters, CompletionProvider}
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.{JsonStringLiteral, JsonFile, JsonProperty}
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext

import scala.annotation.tailrec

protected[idea] case class ContextAwareCompletionProvider(loadKeywords: (String) => List[String]) extends CompletionProvider[CompletionParameters] with CompletionProviderMixin {
  override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
    val keywords = firstNamedProperty(parameters.getPosition).map(_.getName).map(loadKeywords).getOrElse(List()).map(Keyword(_))

    addKeywordsToResult(keywords)(parameters, result)
  }

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
}

protected[idea] case class KeywordsCompletionProvider(keywords: Keywords) extends CompletionProvider[CompletionParameters] with CompletionProviderMixin {
  override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
    addKeywordsToResult(keywords())(parameters, result)
  }
}

protected[idea] trait CompletionProviderMixin {
  protected def addKeywordsToResult(keywords: Iterable[Keyword])(parameters: CompletionParameters, result: CompletionResultSet) {
    keywords.foreach(k => result.addElement(LookupElementBuilder.create(k.text).withInsertHandler(insertHandler(parameters.getPosition, k))))
  }

  protected def insertHandler(element: PsiElement, keyword: Keyword) = {
    if(element.getParent.isInstanceOf[JsonStringLiteral] || !keyword.quoted) null
    else QuoteInsertHandler
  }
}