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
import org.psliwa.idea.composerJson.composer.repository.{InMemoryRepository, Repository, RepositoryInfo, RepositoryProvider}
import org.psliwa.idea.composerJson.intellij.PsiElements._

class RepositoryUpdater extends Annotator {
  import RepositoryUpdater._

  override def annotate(element: PsiElement, holder: AnnotationHolder): Unit = {
    if(pattern.accepts(element)) {
      val notifications = Option(element.getProject.getComponent(classOf[EditorNotifications]))
      val urls = getComposerRepositoryUrls(element)
      val repository = getInlineRepository(element)
      val packagistEnabled = isPackagistEnabled(element)

      getRepositoryProvider(element.getProject)
        .map(
          _.updateRepository(getFilePath(element), new RepositoryInfo(urls, packagistEnabled, Some(repository)))
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

  private def getJsonPropertyValue(objectElement: JsonObject, propertyName: String): Option[JsonElement] = {
    for {
      packageProperty <- Option(objectElement.findProperty(propertyName))
      packageValue <- Option(packageProperty.getValue)
    } yield packageValue
  }

  private def getInlineRepository(element: PsiElement): Repository[String] = {
    val packages = for {
      repositoriesElement <- repositoriesJsonArray(element)
      (pkg, version) <- getPackages(repositoriesElement)
    } yield (pkg, version)

    def update[A,B](map: Map[A,List[B]], pair: (A,B)): Map[A,List[B]] = {
      map + (pair._1 -> (pair._2 :: map.getOrElse(pair._1, List[B]())))
    }

    val packagesMap = packages.foldLeft(Map[String,List[String]]())(update)

    new InMemoryRepository(packagesMap.map(_._1).toSeq, packagesMap)
  }

  private def getPackages(repositoriesElement: JsonArray): Seq[(String, String)] = {
    //TODO: refactor
    for {
      child <- repositoriesElement.getChildren
      objectElement <- ensureJsonObject(child).toList
      typeProperty <- Option(objectElement.findProperty("type")).toList
      repositoryType <- getStringValue(typeProperty.getValue).toList
      if repositoryType == "package"
      packageValue <- getJsonPropertyValue(objectElement, "package").toList
      packageValue <- ensureJsonObject(packageValue).toList
      packageNameElement <- getJsonPropertyValue(packageValue, "name").toList
      packageName <- getStringValue(packageNameElement).toList
      packageVersionElement <- getJsonPropertyValue(packageValue, "version").toList
      packageVersion <- getStringValue(packageVersionElement).toList
    } yield (packageName, packageVersion)
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
