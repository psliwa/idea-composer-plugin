package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.JsonLanguage
import com.intellij.json.psi.JsonElement
import com.intellij.lang.{ASTNode, Language}
import com.intellij.openapi.util.{TextRange, Key}
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.impl.source.tree.{LeafPsiElement, PsiWhiteSpaceImpl}
import com.intellij.psi._
import com.intellij.psi.search.{SearchScope, GlobalSearchScope}
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composer.parser.{SStringChoice, SObject, Schema, SchemaLoader}

import scala.annotation.tailrec

class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
  private lazy val schema = SchemaLoader.load()

  extend(
    CompletionType.BASIC,
    PlatformPatterns.psiElement(classOf[PsiElement]).withLanguage(JsonLanguage.INSTANCE),
    new CompletionProvider[CompletionParameters]() {
      override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
        if(parameters.getOriginalFile.getName == "composer.json") {
          getCompletionsFor(parameters.getOriginalPosition)
            .foreach(s => result.addElement(
              //TODO: support for quotes
              LookupElementBuilder.create(s)
            ))
        }
      }
    }
  )

  //TODO
  private def getSchemaFor(e: PsiElement): Option[Schema] = schema

  private def getCompletionsFor(e: PsiElement): List[String] = {
    getSchemaFor(e)
    .map {
      case SObject(m) => m.keys.toList
      case SStringChoice(m) => m
      case _ => List()
    }
    .getOrElse(List())
  }

  private def getJsonPath(e: PsiElement): List[JsonElement] = {
    @tailrec
    def loop(e: PsiElement, l: List[JsonElement]): List[JsonElement] = {
      e match {
        case j: JsonElement => loop(e.getParent, l :+ j)
        case _ if e.getPrevSibling != null && (e.getPrevSibling.getText == "," || e.getPrevSibling.getText == "{" || e.getPrevSibling.getText == "[" || e.getPrevSibling.getText == ":") => loop(e.getParent, l)
        case _ => l
      }
    }

    loop(e, List())
  }
}
