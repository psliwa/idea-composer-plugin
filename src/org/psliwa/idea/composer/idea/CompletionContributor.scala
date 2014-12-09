package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.{LookupElement, LookupElementBuilder}
import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.StandardPatterns._
import com.intellij.patterns.{PatternCondition, PsiElementPattern}
import com.intellij.psi._
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composer.packagist.Packagist
import org.psliwa.idea.composer.schema._

class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
  private lazy val schema = SchemaLoader.load()
  private val emptyNamePlaceholder = "IntellijIdeaRulezzz"

  private var loadPackages: () => List[String] = () => Packagist.load().right.toOption.getOrElse(List())

  private lazy val packages = loadPackages().map(Keyword(_))

  type Capture = PsiElementPattern.Capture[_ <: PsiElement]

  private def loop(s: Schema, parent: Capture): List[(Capture, CompletionProvider[CompletionParameters])] = s match {
    case SObject(m) => {
      val propertyCapture = psiElement(classOf[JsonProperty]).withParent(psiElement(classOf[JsonObject]).withParent(parent))

      List(
        (
          psiElement().withSuperParent(2, psiElement().and(propertyCapture)
            .andOr(
              psiElement().withName(string().`with`(new PatternCondition[String]("contains") {
                override def accepts(t: String, context: ProcessingContext): Boolean = t.contains(emptyNamePlaceholder)
              })),
              psiElement().withName(emptyNamePlaceholder)
            )
          )
          ,
          KeywordsCompletionProvider(m.keys.map(Keyword(_)))
        )
      ) ++  m.flatMap(t => loop(t._2, psiElement().and(propertyCapture).withName(t._1)))
    }
    case SStringChoice(m) => List((psiElement().withSuperParent(2, parent), KeywordsCompletionProvider(m.map(Keyword(_)))))
    case SOr(l) => l.flatMap(loop(_, parent))
    case SArray(i) => loop(i, psiElement(classOf[JsonArray]).withParent(parent))
    case SBoolean => List((psiElement().withSuperParent(2, parent).afterLeaf(":"), KeywordsCompletionProvider(List("true", "false").map(Keyword(_, quoted = false)))))
    case SPackages => {
      //TODO: refactoring with SObject - SPackages should be special case of SObject?
      val propertyCapture = psiElement(classOf[JsonProperty]).withParent(psiElement(classOf[JsonObject]).withParent(parent))
      List(
        (
          psiElement().withSuperParent(2, psiElement().and(propertyCapture)
            .andOr(
              psiElement().withName(string().`with`(new PatternCondition[String]("contains") {
                override def accepts(t: String, context: ProcessingContext): Boolean = t.contains(emptyNamePlaceholder)
              })),
              psiElement().withName(emptyNamePlaceholder)
            )
          )
          ,
          KeywordsCompletionProvider(packages)
          )
      )
    }
    case _ => List()
  }

  schema.foreach(loop(_,
    psiElement(classOf[JsonFile])
      .withLanguage(JsonLanguage.INSTANCE)
      .inFile(psiFile(classOf[JsonFile]).withName("composer.json"))
  ).foreach {
    case (pattern, provider) => extend(CompletionType.BASIC, pattern, provider)
  })

  private case class Keyword(text: String, quoted: Boolean = true)

  private case class KeywordsCompletionProvider(keywords: Iterable[Keyword]) extends CompletionProvider[CompletionParameters] {
    override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
      keywords.foreach(k => result.addElement(LookupElementBuilder.create(k.text).bold.withInsertHandler(insertHandler(parameters.getPosition, k))))
    }

    private def insertHandler(element: PsiElement, keyword: Keyword) = {
      if(element.getParent.isInstanceOf[JsonStringLiteral] || !keyword.quoted) null
      else QuoteInsertHandler
    }
  }

  private object QuoteInsertHandler extends InsertHandler[LookupElement] {
    override def handleInsert(context: InsertionContext, item: LookupElement): Unit = {
      val document = context.getEditor.getDocument
      val editor = context.getEditor

      document.insertString(context.getStartOffset, "\"")
      document.insertString(context.getStartOffset + 1 + item.getLookupString.length, "\"")

      editor.getCaretModel.moveToOffset(context.getStartOffset + item.getLookupString.length + 2)
    }
  }

  protected[idea] def setPackagesLoader(l: () => List[String]): Unit = {
    loadPackages = l
  }
}
