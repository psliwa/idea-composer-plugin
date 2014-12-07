package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.lang.{ASTNode, Language}
import com.intellij.openapi.util.{TextRange, Key}
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.{PsiElementPattern, PlatformPatterns}
import com.intellij.psi.impl.source.tree.{LeafPsiElement, PsiWhiteSpaceImpl}
import com.intellij.psi._
import com.intellij.psi.search.{SearchScope, GlobalSearchScope}
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composer.parser._

import scala.annotation.tailrec

class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
  private lazy val schema = SchemaLoader.load()
  private val emptyNamePlaceholder = "IntellijIdeaRulezzz"

  type Capture = PsiElementPattern.Capture[_ <: PsiElement]

  private def loop(s: Schema, parent: Capture): List[(Capture, CompletionProvider[CompletionParameters])] = s match {
    case SObject(m) => {
      val propertyCapture = psiElement(classOf[JsonProperty]).withParent(psiElement(classOf[JsonObject]).withParent(parent))

      List(
        (psiElement().withSuperParent(2,
          psiElement().and(propertyCapture).andOr(
            psiElement().withName(emptyNamePlaceholder),
            psiElement().withName(emptyNamePlaceholder+" ")
          )),
          KeywordsCompletionProvider(m.keys))
      ) ++  m.flatMap(t => loop(t._2, psiElement().and(propertyCapture).withName(t._1)))
    }
    case SStringChoice(m) => List((psiElement().withSuperParent(2, parent), KeywordsCompletionProvider(m)))
    case SOr(l) => l.flatMap(loop(_, parent))
    case SArray(i) => loop(i, psiElement(classOf[JsonArray]).withParent(parent))
    case SBoolean => List((psiElement().withSuperParent(2, parent).afterLeaf(":"), KeywordsCompletionProvider(List("true", "false"))))
    case _ => List()
  }

  schema.foreach(loop(_,
    psiElement(classOf[JsonFile])
      .withLanguage(JsonLanguage.INSTANCE)
      .inFile(psiFile(classOf[JsonFile]).withName("composer.json"))
  ).foreach {
    case (pattern, provider) => extend(CompletionType.BASIC, pattern, provider)
  })

  private case class KeywordsCompletionProvider(keywords: Iterable[String]) extends CompletionProvider[CompletionParameters] {
    override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
      keywords.foreach(k => result.addElement(LookupElementBuilder.create(k).bold))
    }
  }
}
