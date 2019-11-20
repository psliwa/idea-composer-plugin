package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.patterns.PlatformPatterns._
import com.intellij.psi.PsiElement
import com.intellij.ui.EditorNotifications
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.composer.model.repository.{Repository, RepositoryInfo, RepositoryProvider}
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
          _.updateRepository(getFilePath(element), RepositoryInfo(urls, packagistEnabled, Some(repository)))
        )
        .filter(_ == true)
        .flatMap(_ => notifications)
        .foreach(_.updateNotifications(element.getContainingFile.getVirtualFile))
    }
  }

  private def getFilePath(element: PsiElement) = getFile(element).getCanonicalPath
  private def getFile(element: PsiElement) = element.getContainingFile.getVirtualFile

  private def getComposerRepositoryUrls(element: PsiElement): List[String] = for {
    repositoriesElement <- repositoriesJsonArray(element)
    url <- getRepositoryUrls(repositoriesElement)
  } yield url

  private def getJsonPropertyValue(objectElement: JsonObject, propertyName: String): Option[JsonElement] = {
    for {
      packageProperty <- findProperty(objectElement, propertyName)
      packageValue <- Option(packageProperty.getValue)
    } yield packageValue
  }

  private def getJsonPropertyValue[A](objectElement: JsonObject, propertyName: String, f: JsonElement => Option[A]): Option[A] = {
    getJsonPropertyValue(objectElement, propertyName).flatMap(f)
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

    Repository.inMemory(packagesMap.keys.toSeq, packagesMap)
  }

  private def getPackages(repositoriesElement: JsonArray): Seq[(String, String)] = {
    def mapPackage(objectElement: JsonObject): Option[(String,String)] = {
      for {
        packageValue <- getJsonPropertyValue(objectElement, "package", ensureJsonObject)
        packageName <- getJsonPropertyValue(packageValue, "name", getStringValue)
        packageVersion <- getJsonPropertyValue(packageValue, "version", getStringValue)
      } yield (packageName, packageVersion)
    }

    mapRepositoryElements(repositoriesElement, "package", mapPackage)
  }

  private def mapRepositoryElements[A](repositoriesElement: JsonArray, requiredRepositoryType: String, f: JsonObject => Option[A]): Seq[A] = {
    for {
      child <- repositoriesElement.getChildren.toList
      objectElement <- ensureJsonObject(child).toList
      repositoryType <- getJsonPropertyValue(objectElement, "type", getStringValue).toList
      if repositoryType == requiredRepositoryType
      value <- f(objectElement).toList
    } yield value
  }

  private def isPackagistEnabled(element: PsiElement): Boolean = {
    val packagistEnabledFlags = for {
      repositoriesElement <- repositoriesJsonArray(element)
      child <- repositoriesElement.getChildren
      objectElement <- ensureJsonObject(child).toList
      packagistEnabled <- getJsonPropertyValue(objectElement, "packagist", getBooleanValue)
    } yield packagistEnabled

    !packagistEnabledFlags.contains(false)
  }

  private def repositoriesJsonArray(element: PsiElement): List[JsonArray] = {
    for {
      obj <- ensureJsonObject(element).toList
      repositoriesElement <- getJsonPropertyValue(obj, "repositories", ensureJsonArray).toList
    } yield repositoriesElement
  }

  private def getRepositoryProvider(project: Project): Option[RepositoryProvider[_]] = {
    Option(ApplicationManager.getApplication.getComponent(classOf[PackagesLoader])).map(_.repositoryProviderFor(project))
  }

  private def getRepositoryUrls(repositoriesElement: JsonArray): Iterable[String] = {
    def mapUrl(objectElement: JsonObject): Option[String] = {
      getJsonPropertyValue(objectElement, "url", getStringValue).map(_ + "/packages.json")
    }

    def mapPath(objectElement: JsonObject): Option[String] = {
      getJsonPropertyValue(objectElement, "url", getStringValue).flatMap { path =>
        path.headOption match {
          case Some('/') =>
            Some(s"file://${path.stripSuffix("/")}/composer.json")
          case _ =>
            Option(objectElement.getContainingFile.getContainingDirectory)
              .map(_.getVirtualFile.getUrl + s"/${path.stripSuffix("/")}/composer.json")
        }
      }.map(fixFileUrl)
    }

    def fixFileUrl(url: String): String = {
      if(SystemInfo.isWindows && "file://[^/]".r.findFirstMatchIn(url).isDefined) {
        url.replaceFirst("file://", "file:///")
      } else {
        url
      }
    }

    mapRepositoryElements(repositoriesElement, "composer", mapUrl).toList ::: mapRepositoryElements(repositoriesElement, "path", mapPath).toList
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
