package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns._
import com.intellij.psi.PsiElement
import com.intellij.ui.EditorNotifications
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.composer.repository.{RepositoryInfo, RepositoryProvider}
import org.psliwa.idea.composerJson.intellij.PsiElements._

class RepositoryUpdater extends Annotator {
  import RepositoryUpdater._

  override def annotate(element: PsiElement, holder: AnnotationHolder): Unit = {
    if(pattern.accepts(element)) {
      val notifications = Option(element.getProject.getComponent(classOf[EditorNotifications]))
      val urls = getComposerRepositoryUrls(element)
      val packagistEnabled = isPackagistEnabled(element)

      getRepositoryProvider(element.getProject)
        .map(
          _.updateRepository(getFilePath(element), new RepositoryInfo(urls, packagistEnabled))
        )
        .filter(_ == true)
        .flatMap(_ => notifications)
        .foreach(_.updateNotifications(element.getContainingFile.getVirtualFile))
    }
  }

  private def getFilePath(element: PsiElement) = getFile(element).getCanonicalPath
  private def getFile(element: PsiElement) = element.getContainingFile.getVirtualFile

  private def getComposerRepositoryUrls(element: PsiElement): List[String] = {
    val urls = for {
      repositoriesElement <- repositoriesJsonArray(element)
      url <- getRepositoryUrls(repositoriesElement)
    } yield url
    urls
  }

  private def isPackagistEnabled(element: PsiElement): Boolean = {
    val packagistEnabledFlags = for {
      repositoriesElement <- repositoriesJsonArray(element)
      child <- repositoriesElement.getChildren
      objectElement <- ensureJsonObject(child).toList
      packagistProperty <- Option(objectElement.findProperty("packagist")).toList
      packagistEnabled <- getBooleanValue(packagistProperty.getValue)
    } yield packagistEnabled

    !packagistEnabledFlags.contains(false)
  }

  private def repositoriesJsonArray(element: PsiElement): List[JsonArray] = {
    for {
      obj <- ensureJsonObject(element).toList
      repositoriesProperty <- Option(obj.findProperty("repositories")).toList
      repositoriesElement <- ensureJsonArray(repositoriesProperty.getValue).toList
    } yield repositoriesElement
  }

  private def getRepositoryProvider(project: Project): Option[RepositoryProvider[_]] = {
    Option(ApplicationManager.getApplication.getComponent(classOf[PackagesLoader])).map(_.repositoryProviderFor(project))
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
  val pattern = psiElement(classOf[JsonObject])
    .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
    .withLanguage(JsonLanguage.INSTANCE)
    .withParent(
      psiFile(classOf[JsonFile])
    )
}
