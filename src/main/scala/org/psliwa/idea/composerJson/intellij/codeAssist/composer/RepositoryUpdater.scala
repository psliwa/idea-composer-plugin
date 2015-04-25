package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.patterns.PlatformPatterns._
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.composer.repository.{RepositoryInfo, RepositoryProvider}
import org.psliwa.idea.composerJson.intellij.PsiElements._

class RepositoryUpdater extends Annotator {
  import RepositoryUpdater._

  override def annotate(element: PsiElement, holder: AnnotationHolder): Unit = {
    if(pattern.accepts(element)) {
      val urls = getComposerRepositoryUrls(element)
      val packagistEnabled = isPackagistEnabled(element)

      getRepositoryProvider.foreach(repositoryProvider => {
        repositoryProvider.updateRepository(element.getContainingFile.getVirtualFile.getCanonicalPath, new RepositoryInfo(urls, packagistEnabled))
      })
    }
  }

  private def getComposerRepositoryUrls(element: PsiElement): List[String] = {
    val urls = for {
      repositoriesElement <- ensureJsonArray(element).toList
      url <- getRepositoryUrls(repositoriesElement)
    } yield url
    urls
  }

  private def isPackagistEnabled(element: PsiElement): Boolean = {
    val packagistEnabledFlags = for {
      repositoriesElement <- ensureJsonArray(element).toList
      child <- repositoriesElement.getChildren
      objectElement <- ensureJsonObject(child).toList
      packagistProperty <- Option(objectElement.findProperty("packagist")).toList
      packagistEnabled <- getBooleanValue(packagistProperty.getValue)
    } yield packagistEnabled

    !packagistEnabledFlags.contains(false)
  }

  private def getRepositoryProvider: Option[RepositoryProvider[_]] = {
    Option(ApplicationManager.getApplication.getComponent(classOf[PackagesLoader])).map(_.repositoryProvider)
  }

  private def getRepositoryUrls(repositoriesElement: JsonArray): Iterable[String] = {
    import scala.collection.JavaConversions._

    for {
      child <- repositoriesElement.getChildren
      objectElement <- ensureJsonObject(child).toList
      typeProperty <- Option(objectElement.findProperty("type")).toList
      repositoryType <- getStringValue(typeProperty.getValue).toList
      if repositoryType == "composer"
      urlProperty <- Option(objectElement.findProperty("url")).toList
      url <- getStringValue(urlProperty.getValue).toList
    } yield url+"/packages.json"
  }
}

private object RepositoryUpdater {
  val pattern = psiElement(classOf[JsonArray])
    .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
    .withLanguage(JsonLanguage.INSTANCE)
    .withParent(
      psiElement(classOf[JsonProperty]).withName("repositories")
    )
}
