package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.json.JsonLanguage
import com.intellij.json.psi.{JsonArray, JsonFile, JsonObject, JsonProperty}
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.{StringPattern, PsiElementPattern}
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.intellij.Patterns._
import org.psliwa.idea.composerJson.json._
import com.intellij.patterns.StandardPatterns.string

import scala.annotation.tailrec
import scala.collection.Seq

abstract class AbstractCompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {

  protected lazy val maybeSchema = ComposerSchema

  maybeSchema.foreach(addCompletionProvidersForSchema)

  import org.psliwa.idea.composerJson.intellij.codeAssist.AbstractCompletionContributor._

  private def addCompletionProvidersForSchema(schema: Schema): Unit = {
    completionProvidersForSchema(schema, rootPsiElementPattern).foreach {
      case (pattern, provider) => extend(CompletionType.BASIC, pattern, provider)
    }
  }

  private def completionProvidersForSchema(s: Schema, parent: Capture): List[(Capture, CompletionProvider[CompletionParameters])] = s match {
    case SObject(properties, _) => {
      propertyCompletionProvider(parent, properties.named) ++
        completionProvidersForProperties(properties.named, parent, string().equalTo(_: String)) ++
        completionProvidersForProperties(properties.patterned, parent, stringMatches)
    }
    case SOr(l) => l.flatMap(completionProvidersForSchema(_, parent))
    case SArray(i) => completionProvidersForSchema(i, psiElement(classOf[JsonArray]).withParent(parent))
    case _ => getCompletionProvidersForSchema(s, parent)
  }

  private def completionProvidersForProperties[Name](properties: Map[Name,Property], parent: Capture, namePattern: Name => StringPattern) = {
    properties.flatMap(t => completionProvidersForSchema(t._2.schema, psiElement().and(propertyCapture(parent)).withName(namePattern(t._1))))
  }

  protected def propertyCompletionProvider(parent: Capture, properties: Map[String, Property]): List[(Capture, CompletionProvider[CompletionParameters])] = List()

  protected def insertHandlerFor(schema: Schema): Option[InsertHandler[LookupElement]] = None

  protected def getCompletionProvidersForSchema(s: Schema, parent: Capture): List[(Capture, CompletionProvider[CompletionParameters])]

  protected def propertyCompletionProvider(
    parent: Capture, completionProvider: CompletionProvider[CompletionParameters]
  ): List[(Capture, CompletionProvider[CompletionParameters])] = {
    List((
      psiElement()
        .withSuperParent(2,
          psiElement().and(propertyCapture(parent))
            .withName(stringContains(EmptyNamePlaceholder))
        ),
      completionProvider
    ))
  }

  protected def propertyCompletionProvider(
    parent: Capture, es: LookupElements, getInsertHandler: InsertHandlerFinder = _ => None
  ): List[(Capture, CompletionProvider[CompletionParameters])] = {
    propertyCompletionProvider(parent, PropertyCompletionProvider(es, getInsertHandler))
  }

  private def rootPsiElementPattern: PsiElementPattern.Capture[JsonFile] = {
    psiElement(classOf[JsonFile])
      .withLanguage(JsonLanguage.INSTANCE)
      .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
  }

  private[intellij] def propertyCapture(parent: Capture): PsiElementPattern.Capture[JsonProperty] = {
    psiElement(classOf[JsonProperty]).withParent(psiElement(classOf[JsonObject]).withParent(parent))
  }
}

object AbstractCompletionContributor {

  private object PropertyCompletionProvider {
    def apply(es: LookupElements, getInsertHandler: InsertHandlerFinder) = {
      new ParametersDependantCompletionProvider(context => {
        import scala.collection.JavaConversions._

        val existingProperties: Set[String] = for {
          obj <- firstJsonObject(context.getPosition).toSet[JsonObject]
          property <- obj.getPropertyList
        } yield property.getName

        es(context)
          .filter(element => !existingProperties.contains(element.getLookupString))
          .toSeq
      }, getInsertHandler)
    }
  }

  @tailrec
  private def firstJsonObject(element: PsiElement): Option[JsonObject] = element match {
    case x: JsonObject => Some(x)
    case x: PsiElement => firstJsonObject(x.getParent)
    case null => None
  }

  class ParametersDependantCompletionProvider(loadElements: CompletionParameters => Seq[BaseLookupElement], getInsertHandler: InsertHandlerFinder = _ => None) extends AbstractCompletionProvider {
    override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
      val es = loadElements(parameters)

      addLookupElementsToResult(es, getInsertHandler)(parameters, mapResult(result))
    }

    protected def mapResult(result: CompletionResultSet) = result
  }

  abstract class AbstractCompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider[CompletionParameters] {
    protected def addLookupElementsToResult(es: Iterable[BaseLookupElement], getInsertHandler: InsertHandlerFinder = _ => None)
        (parameters: CompletionParameters, result: CompletionResultSet) {

      es.toList.reverse.foreach(e => {
        val item = e.withPsiElement(parameters.getPosition).withInsertHandler(insertHandler(parameters.getPosition, e, getInsertHandler))
        result.addElement(item.priority.map(PrioritizedLookupElement.withPriority(item, _)).getOrElse(item))
      })
    }

    protected def insertHandler(element: PsiElement, le: BaseLookupElement, getInsertHandler: InsertHandlerFinder) = {
      if(!le.quoted) null
      else getInsertHandler(le).getOrElse(QuoteInsertHandler)
    }
  }

  class LookupElementsCompletionProvider(es: LookupElements, getInsertHandler: InsertHandlerFinder = _ => None)
    extends AbstractCompletionProvider {

    override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
      addLookupElementsToResult(es(parameters), getInsertHandler)(parameters, result)
    }
  }
}
