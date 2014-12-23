package org.psliwa.idea.composerJson.completion

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.StandardPatterns._
import com.intellij.patterns.{PsiElementPattern, PatternCondition}
import com.intellij.psi.{PsiDirectory, PsiElement}
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson.composer._
import org.psliwa.idea.composerJson.util.CharType
import org.psliwa.idea.composerJson.util.Funcs._
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.util.CharType._
import org.psliwa.idea.composerJson.util.CharType.ImplicitConversions._

import scala.annotation.tailrec
import scala.collection.Seq

class CompletionContributor extends com.intellij.codeInsight.completion.CompletionContributor {

  private lazy val schema = SchemaLoader.load(ComposerSchemaFilepath)

  //vars only for testability
  private var packagesLoader: () => Seq[BaseLookupElement] = () => PackagesLoader.loadPackages
  private var versionsLoader: (String) => Seq[String] = memorize(30)(Packagist.loadVersions(_).right.getOrElse(List()))

  schema.foreach(addCompletionProvidersForSchema)

  private def addCompletionProvidersForSchema(schema: Schema): Unit = {
    completionProvidersForSchema(schema, rootPsiElementPattern).foreach {
      case (pattern, provider) => extend(CompletionType.BASIC, pattern, provider)
    }
  }

  import CompletionContributor._
  private def completionProvidersForSchema(s: Schema, parent: Capture): List[(Capture, CompletionProvider[CompletionParameters])] = s match {
    case SObject(m, _) => {
      propertyCompletionProvider(parent, () => m.keys.map(BaseLookupElement(_)), (k) => insertHandlerFor(m.get(k.name).get.schema)) ++
        m.flatMap(t => completionProvidersForSchema(t._2.schema, psiElement().and(propertyCapture(parent)).withName(t._1)))
    }
    case SStringChoice(m) => List((psiElement().withSuperParent(2, parent), new LookupElementsCompletionProvider(() => m.map(BaseLookupElement(_)))))
    case SOr(l) => l.flatMap(completionProvidersForSchema(_, parent))
    case SArray(i) => completionProvidersForSchema(i, psiElement(classOf[JsonArray]).withParent(parent))
    case SPackages => {
      propertyCompletionProvider(parent, loadPackages, _ => Some(StringPropertyValueInsertHandler)) ++
        List((
          psiElement().withSuperParent(2, psiElement().and(propertyCapture(parent))).afterLeaf(":"),
          new VersionCompletionProvider(c => {
            loadVersions(c.propertyName).flatMap(Version.alternativesForPrefix(c.typedQuery)).map(BaseLookupElement(_, Option(Icons.Packagist)))
          })
        ))
    }
    case SFilePath => {
      List((psiElement().withSuperParent(2, parent), FilePathProvider))
    }
    case SFilePaths => {
      List((psiElement().withSuperParent(2, psiElement().and(propertyCapture(parent))).afterLeaf(":"), FilePathProvider))
    }
    case _ => List()
  }

  private def rootPsiElementPattern: PsiElementPattern.Capture[JsonFile] = {
    psiElement(classOf[JsonFile])
      .withLanguage(JsonLanguage.INSTANCE)
      .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
  }

  private def propertyCompletionProvider(parent: Capture, es: LookupElements, getInsertHandler: InsertHandlerFinder = _ => None) = {
    List((
      psiElement()
        .withSuperParent(2,
          psiElement().and(propertyCapture(parent))
            .withName(stringContains(EmptyNamePlaceholder))
        ),
      new LookupElementsCompletionProvider(es, getInsertHandler)
    ))
  }

  @tailrec
  private def insertHandlerFor(schema: Schema): Option[InsertHandler[LookupElement]] = schema match {
    case SString(_) | SStringChoice(_) => Some(StringPropertyValueInsertHandler)
    case SObject(_, _) | SPackages | SFilePaths => Some(ObjectPropertyValueInsertHandler)
    case SArray(_) => Some(ArrayPropertyValueInsertHandler)
    case SBoolean | SNumber => Some(EmptyPropertyValueInsertHandler)
    case SOr(h::_) => insertHandlerFor(h)
    case _ => None
  }

  private def propertyCapture(parent: Capture): PsiElementPattern.Capture[JsonProperty] = {
    psiElement(classOf[JsonProperty]).withParent(psiElement(classOf[JsonObject]).withParent(parent))
  }

  protected[completion] def setPackagesLoader(l: () => Seq[BaseLookupElement]): Unit = {
    packagesLoader = l
  }

  protected[completion] def setVersionsLoader(l: (String) => Seq[String]): Unit = {
    versionsLoader = l
  }

  private def loadPackages() = packagesLoader()
  private def loadVersions(s: String) = versionsLoader(s)

  private def stringContains(s: String) = {
    string().`with`(new PatternCondition[String]("contains") {
      override def accepts(t: String, context: ProcessingContext): Boolean = t.contains(s)
    })
  }
}

protected[completion] object CompletionContributor {

  //completion providers

  abstract class AbstractCompletionProvider extends com.intellij.codeInsight.completion.CompletionProvider[CompletionParameters] {
    protected def addLookupElementsToResult(es: Iterable[BaseLookupElement], getInsertHandler: InsertHandlerFinder = _ => None)
        (parameters: CompletionParameters, result: CompletionResultSet) {

      es.foreach(e => {
        result.addElement(e.withPsiElement(parameters.getPosition).withInsertHandler(insertHandler(parameters.getPosition, e, getInsertHandler)))
      })
    }

    protected def insertHandler(element: PsiElement, le: BaseLookupElement, getInsertHandler: InsertHandlerFinder) = {
      if(!le.quoted) null
      else getInsertHandler(le).getOrElse(QuoteInsertHandler)
    }
  }

  class ParametersDependantCompletionProvider(loadElements: CompletionParameters => Seq[BaseLookupElement]) extends AbstractCompletionProvider {
    override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
      val es = loadElements(parameters)

      addLookupElementsToResult(es)(parameters, mapResult(result))
    }

    protected def mapResult(result: CompletionResultSet) = result
  }

  object FilePathProvider extends ParametersDependantCompletionProvider(parameters => {
    val result = for {
      text <- getTypedText(parameters.getPosition).map(stripQuotes).orElse(Some(""))
      dirPath <- dirPath(text)
      rootDir <- Option(parameters.getOriginalFile.getParent)
      subDir <- findDir(rootDir, dirPath)
    } yield {
      subDir.getFiles.map(BaseLookupElement(_)).toList ++ subDir.getSubdirectories.map(BaseLookupElement(_))
    }

    result.getOrElse(List())
  }){
    override protected def mapResult(result: CompletionResultSet): CompletionResultSet = {
      val prefix = result.getPrefixMatcher.getPrefix
      val matcher = createCharContainsMatcher('/')(prefix)

      result.withPrefixMatcher(matcher)
    }

    override protected def insertHandler(element: PsiElement, le: BaseLookupElement,getInsertHandler: InsertHandlerFinder) = {
      Option(super.insertHandler(element, le, getInsertHandler))
        .map(handler => new AutoPopupInsertHandler(Some(handler)))
        .orNull
    }
  }

  class LookupElementsCompletionProvider(es: LookupElements, getInsertHandler: InsertHandlerFinder = _ => None)
    extends AbstractCompletionProvider {

    override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
      addLookupElementsToResult(es(), getInsertHandler)(parameters, result)
    }
  }

  import VersionCompletionProvider._
  class VersionCompletionProvider(loadElements: Context => Seq[BaseLookupElement]) extends ParametersDependantCompletionProvider(psiBased(loadElements)) {
    override protected def mapResult(result: CompletionResultSet): CompletionResultSet = {
      val prefix = result.getPrefixMatcher.getPrefix
      val matcher = createCharContainsMatcher(' ' || '~' || '^' || ',' || '>' || '<' || '=')(prefix)

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

  //utility functions

  private def createCharContainsMatcher(stopChar: CharType)(prefix: String) = {
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
      case p@JsonProperty(name) => Some(p)
      case _: JsonFile => None
      case e => firstNamedProperty(e.getParent)
    }
  }

  private object JsonProperty {
    def unapply(x: JsonProperty): Option[(String)] = if(x.getName.contains(EmptyNamePlaceholder)) None else Some(x.getName)
  }

  private object LeafPsiElement {
    def unapply(x: LeafPsiElement): Option[(String)] = Some(x.getText)
  }

  private def stripQuotes(s: String) = s.stripPrefix("\"").stripSuffix("\"")

  private def dirPath(s: String): Option[String] = {
    findOffsetReverse('/')(s.length-1)(s)
      .map(s.substring(0, _))
      .orElse(Some(""))
  }

  private def findDir(rootDir: PsiDirectory, path: String): Option[PsiDirectory] = {
    @tailrec
    def loop(rootDir: PsiDirectory, paths: List[String]): Option[PsiDirectory] = {
      paths match {
        case Nil => Some(rootDir)
        case h::t => {
          val subDir = rootDir.findSubdirectory(h)

          if(subDir == null) None
          else loop(subDir, t)
        }
      }
    }

    loop(rootDir, path.split("/").toList.filter(!_.isEmpty))
  }
}