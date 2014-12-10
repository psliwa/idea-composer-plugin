package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.{LookupElement, LookupElementBuilder}
import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.StandardPatterns._
import com.intellij.patterns.{PsiElementPattern, PatternCondition}
import com.intellij.psi._
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composer.packagist.Packagist
import org.psliwa.idea.composer.schema._

class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {
  import CompletionContributor._

  private val emptyNamePlaceholder = "IntellijIdeaRulezzz"

  private lazy val schema = SchemaLoader.load()
  private lazy val packages = loadPackages().map(Keyword(_))
  private var loadPackages: () => List[String] = () => Packagist.load().right.toOption.getOrElse(List())

  schema.foreach(addCompletionProvidersForSchema)

  private def addCompletionProvidersForSchema(schema: Schema): Unit = {
    completionProvidersForSchema(schema, rootPsiElementPattern).foreach {
      case (pattern, provider) => extend(CompletionType.BASIC, pattern, provider)
    }
  }

  private def completionProvidersForSchema(s: Schema, parent: Capture): List[(Capture, CompletionProvider[CompletionParameters])] = s match {
    case SObject(m) => {
      propertyCompletionProviders(parent, () => m.keys.map(Keyword(_))) ++
        m.flatMap(t => completionProvidersForSchema(t._2, psiElement().and(propertyCapture(parent)).withName(t._1)))
    }
    case SStringChoice(m) => List((psiElement().withSuperParent(2, parent), KeywordsCompletionProvider(() => m.map(Keyword(_)))))
    case SOr(l) => l.flatMap(completionProvidersForSchema(_, parent))
    case SArray(i) => completionProvidersForSchema(i, psiElement(classOf[JsonArray]).withParent(parent))
    case SBoolean => List((psiElement().withSuperParent(2, parent).afterLeaf(":"), KeywordsCompletionProvider(() => List("true", "false").map(Keyword(_, quoted = false)))))
    case SPackages => {
      propertyCompletionProviders(parent, () => packages)
    }
    case _ => List()
  }

  private def rootPsiElementPattern: PsiElementPattern.Capture[JsonFile] = {
    psiElement(classOf[JsonFile])
      .withLanguage(JsonLanguage.INSTANCE)
      .inFile(psiFile(classOf[JsonFile]).withName("composer.json"))
  }

  private def propertyCompletionProviders(parent: Capture, keywords: Keywords) = {
    List(
      (
        psiElement()
          .withSuperParent(2,
            psiElement().and(propertyCapture(parent))
              .withName(stringContains(emptyNamePlaceholder))
          ),
        KeywordsCompletionProvider(keywords)
      )
    )
  }

  private def propertyCapture(parent: Capture): PsiElementPattern.Capture[JsonProperty] = {
    psiElement(classOf[JsonProperty]).withParent(psiElement(classOf[JsonObject]).withParent(parent))
  }

  protected[idea] def setPackagesLoader(l: () => List[String]): Unit = {
    loadPackages = l
  }

  private def stringContains(s: String) = {
    string().`with`(new PatternCondition[String]("contains") {
      override def accepts(t: String, context: ProcessingContext): Boolean = t.contains(s)
    })
  }
}

private object CompletionContributor {
  type Capture = PsiElementPattern.Capture[_ <: PsiElement]
  type Keywords = () => Iterable[Keyword]

  case class Keyword(text: String, quoted: Boolean = true)

  case class KeywordsCompletionProvider(keywords: Keywords) extends CompletionProvider[CompletionParameters] {
    override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
      keywords().foreach(k => result.addElement(LookupElementBuilder.create(k.text).bold.withInsertHandler(insertHandler(parameters.getPosition, k))))
    }

    private def insertHandler(element: PsiElement, keyword: Keyword) = {
      if(element.getParent.isInstanceOf[JsonStringLiteral] || !keyword.quoted) null
      else QuoteInsertHandler
    }
  }

  object QuoteInsertHandler extends InsertHandler[LookupElement] {
    override def handleInsert(context: InsertionContext, item: LookupElement): Unit = {
      val document = context.getEditor.getDocument
      val editor = context.getEditor

      document.insertString(context.getStartOffset, "\"")
      document.insertString(context.getStartOffset + 1 + item.getLookupString.length, "\"")

      editor.getCaretModel.moveToOffset(context.getStartOffset + item.getLookupString.length + 2)
    }
  }
}
