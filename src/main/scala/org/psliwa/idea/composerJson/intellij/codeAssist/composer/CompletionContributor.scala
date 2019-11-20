package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.codeInsight.completion._
import com.intellij.json.psi.{JsonFile, JsonProperty}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns._
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.Icons
import org.psliwa.idea.composerJson.composer.model.PackageName
import org.psliwa.idea.composerJson.composer.model.repository._
import org.psliwa.idea.composerJson.composer.model.version.VersionSuggestions
import org.psliwa.idea.composerJson.intellij._
import org.psliwa.idea.composerJson.intellij.codeAssist.AbstractCompletionContributor.{LookupElementsCompletionProvider, ParametersDependantCompletionProvider}
import org.psliwa.idea.composerJson.intellij.codeAssist.{AbstractCompletionContributor, BaseLookupElement, Capture, _}
import org.psliwa.idea.composerJson.json.{SObject, SPackages, SStringChoice, Schema}
import org.psliwa.idea.composerJson.util.CharOffsetFinder._
import org.psliwa.idea.composerJson.util.ImplicitConversions._
import org.psliwa.idea.composerJson.util.OffsetFinder.ImplicitConversions._

import scala.annotation.tailrec

class CompletionContributor extends AbstractCompletionContributor {

  //var only for testability
  private var repositoryProvider: (Project, String) => Repository[BaseLookupElement] = (project, file) => getPackagesLoader
    .map(_.repositoryProviderFor(project))
    .getOrElse(EmptyRepositoryProvider)
    .repositoryFor(file)

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
      propertyCompletionProvider(parent, new LookupElementsCompletionProvider(loadPackages, _ => Some(StringPropertyValueInsertHandler))) ++
        List((
          psiElement().withSuperParent(2, psiElement().and(propertyCapture(parent))).afterLeaf(":"),
          new VersionCompletionProvider(context => {
            val query = context.typedQuery.stripQuotes
            val pattern = "^(?i).*@[a-z]*$".r
            query match {
              case pattern() => minimumStabilities.map(new BaseLookupElement(_))
              case _ => {
                VersionSuggestions
                  .suggestionsForVersions(loadVersions(context.completionParameters)(context.packageName), context.typedQuery, mostSignificantFirst = false)
                  .zipWithIndex
                  .map{ case(version, index) => new BaseLookupElement(version, Option(Icons.Packagist), true, None, None, "", Some(index)) }
              }
            }
          })
        ))
    }
    case _ => List()
  }

  private def loadPackages(context: CompletionParameters): Seq[BaseLookupElement] = {
    repositoryProvider(context.getOriginalFile.getProject, context.getOriginalFile.getVirtualFile.getCanonicalPath).getPackages
  }

  private def loadVersions(context: CompletionParameters)(packageName: PackageName): Seq[String] = {
    repositoryProvider(context.getOriginalFile.getProject, context.getOriginalFile.getVirtualFile.getCanonicalPath).getPackageVersions(packageName)
  }

  protected[composer] def setPackagesLoader(l: () => Seq[BaseLookupElement]): Unit = {
    val previousRepositoryProvider = repositoryProvider
    repositoryProvider = (project: Project, file: String) => {
      createRepository(l, previousRepositoryProvider(project, file).getPackageVersions)
    }
  }

  private def createRepository(packagesLoader: () => Seq[BaseLookupElement], versionsLoader: PackageName => Seq[String]): Repository[BaseLookupElement] = {
    new Repository[BaseLookupElement] {
      override def getPackages: scala.Seq[BaseLookupElement] = packagesLoader()
      override def getPackageVersions(packageName: PackageName): scala.Seq[String] = versionsLoader(packageName)
      override def map[NewPackage](f: (BaseLookupElement) => NewPackage): Repository[NewPackage] = Repository.callback(getPackages.map(f), getPackageVersions)
    }
  }

  protected[composer] def setVersionsLoader(loader: PackageName => Seq[String]): Unit = {
    val previousRepositoryProvider = repositoryProvider
    repositoryProvider = (project: Project, file: String) => {
      createRepository(previousRepositoryProvider(project, file).getPackages _, loader)
    }
  }

  private def ensureSchemaObject(s: Schema): Option[SObject] = s match {
    case x: SObject => Some(x)
    case _ => None
  }

  private def ensureSchemaChoice(s: Schema): Option[SStringChoice] = s match {
    case x: SStringChoice => Some(x)
    case _ => None
  }

  private def getPackagesLoader: Option[PackagesLoader] = {
    Option(ApplicationManager.getApplication.getComponent(classOf[PackagesLoader]))
  }
}

private object CompletionContributor {
  import PsiExtractors._
  import VersionCompletionProvider._

  class VersionCompletionProvider(loadElements: Context => Seq[BaseLookupElement]) extends ParametersDependantCompletionProvider(psiBased(loadElements)) {
    override protected def mapResult(result: CompletionResultSet): CompletionResultSet = {
      val prefix = result.getPrefixMatcher.getPrefix
      val matcher = createCharContainsMatcher(' ' || '~' || '^' || ',' || '>' || '<' || '=' || '@')(prefix)

      result.withPrefixMatcher(matcher)
    }
  }

  object VersionCompletionProvider {
    case class Context(packageName: PackageName, typedQuery: String, completionParameters: CompletionParameters)

    def psiBased(f: Context => Seq[BaseLookupElement]): CompletionParameters => Seq[BaseLookupElement] = parameters => {
      val typedQuery = getTypedText(parameters.getPosition).getOrElse("")
      firstNamedProperty(parameters.getPosition).map(p => Context(PackageName(p.getName), typedQuery, parameters)).map(f).getOrElse(List())
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
      case p@PsiExtractors.JsonProperty(name, _) => Some(p)
      case _: JsonFile => None
      case e => firstNamedProperty(e.getParent)
    }
  }
}
