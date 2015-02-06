package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.codeInsight.completion.{CompletionResultSet, CompletionParameters, CompletionProvider}
import com.intellij.json.psi.{JsonFile, JsonProperty}
import com.intellij.patterns.PlatformPatterns._
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.{intellij, Icons}
import org.psliwa.idea.composerJson.composer.repository.Packagist
import org.psliwa.idea.composerJson.composer.version.Version
import org.psliwa.idea.composerJson.intellij.codeAssist.{BaseLookupElement, AbstractCompletionContributor}
import AbstractCompletionContributor.ParametersDependantCompletionProvider
import org.psliwa.idea.composerJson.intellij.codeAssist.Capture
import org.psliwa.idea.composerJson.intellij._
import intellij.codeAssist._
import org.psliwa.idea.composerJson.json.{SPackages, Schema, SObject, SStringChoice}
import org.psliwa.idea.composerJson.util.CharOffsetFinder._
import org.psliwa.idea.composerJson.util.OffsetFinder.ImplicitConversions._
import org.psliwa.idea.composerJson.util.Funcs._
import org.psliwa.idea.composerJson.util.ImplicitConversions._

import scala.annotation.tailrec
import scala.collection.Seq

class CompletionContributor extends AbstractCompletionContributor {

  //vars only for testability
  private var packagesLoader: () => Seq[BaseLookupElement] = () => PackagesLoader.loadPackages
  private var versionsLoader: (String) => Seq[String] = memorize(30)(Packagist.loadVersions(_).right.getOrElse(List()))

  lazy private val minimumStabilities: List[String] = for {
    schema <- maybeSchema.toList
    obj <- ensureSchemaObject(schema).toList
    prop <- obj.properties.get("minimum-stability").map(_.schema).toList
    minimumStabilities <- ensureSchemaChoice(prop).toList
    minimumStability <- minimumStabilities.choices
  } yield minimumStability

  import CompletionContributor._

  override protected def getCompletionProvidersForSchema(s: Schema, parent: Capture): List[(Capture, CompletionProvider[CompletionParameters])] = s match {
    case SPackages => {
      propertyCompletionProvider(parent, loadPackages, _ => Some(StringPropertyValueInsertHandler)) ++
        List((
          psiElement().withSuperParent(2, psiElement().and(propertyCapture(parent))).afterLeaf(":"),
          new VersionCompletionProvider(context => {
            val query = context.typedQuery.stripQuotes
            val pattern = "^(?i).*@[a-z]*$".r

            query match {
              case pattern() => minimumStabilities.map(BaseLookupElement(_))
              case _ => {
                loadVersions(context.propertyName)
                  .flatMap(Version.alternativesForPrefix(context.typedQuery))
                  .map(BaseLookupElement(_, Option(Icons.Packagist)))
              }
            }
          })
        ))
    }
    case _ => List()
  }


  protected[composer] def setPackagesLoader(l: () => Seq[BaseLookupElement]): Unit = {
    packagesLoader = l
  }

  protected[composer] def setVersionsLoader(l: (String) => Seq[String]): Unit = {
    versionsLoader = l
  }

  private def loadPackages() = packagesLoader()
  private def loadVersions(s: String) = versionsLoader(s)

  private def ensureSchemaObject(s: Schema): Option[SObject] = s match {
    case x: SObject => Some(x)
    case _ => None
  }

  private def ensureSchemaChoice(s: Schema): Option[SStringChoice] = s match {
    case x: SStringChoice => Some(x)
    case _ => None
  }
}

private object CompletionContributor {
  import VersionCompletionProvider._
  import PsiExtractors._

  class VersionCompletionProvider(loadElements: Context => Seq[BaseLookupElement]) extends ParametersDependantCompletionProvider(psiBased(loadElements)) {
    override protected def mapResult(result: CompletionResultSet): CompletionResultSet = {
      val prefix = result.getPrefixMatcher.getPrefix
      val matcher = createCharContainsMatcher(' ' || '~' || '^' || ',' || '>' || '<' || '=' || '@')(prefix)

      result.withPrefixMatcher(matcher)
    }
  }

  object VersionCompletionProvider {
    case class Context(propertyName: String, typedQuery: String)

    def psiBased(f: Context => Seq[BaseLookupElement]): CompletionParameters => Seq[BaseLookupElement] = parameters => {
      val typedQuery = getTypedText(parameters.getPosition).getOrElse("")
      firstNamedProperty(parameters.getPosition).map(p => Context(p.getName, typedQuery)).map(f).getOrElse(List())
    }
  }


  private def createCharContainsMatcher(stopChar: CharMatcher)(prefix: String) = {
    val fixedPrefix = findOffsetReverse(stopChar)(prefix.length-1)(prefix)
      .map(offset => prefix.substring(offset + 1))
      .getOrElse(prefix)

    new CharContainsMatcher(fixedPrefix)
  }

  private def getTypedText(e: PsiElement): Option[String] = e match {
    case LeafPsiElement(text) => Some(text).map(removeEmptyPalceholder)
    case _ => None
  }

  private def removeEmptyPalceholder(s: String) = s.replace(EmptyNamePlaceholder+" ", "").replace(EmptyNamePlaceholder, "")

  @tailrec
  private def firstNamedProperty(element: PsiElement): Option[JsonProperty] = {
    element match {
      case p@PsiExtractors.JsonProperty(name) => Some(p)
      case _: JsonFile => None
      case e => firstNamedProperty(e.getParent)
    }
  }
}
