package org.psliwa.idea.composerJson.composer.command

import com.intellij.openapi.project.Project
import org.psliwa.idea.composerJson.settings.ProjectSettings

import scala.util.Try

final case class Configuration(executable: String, commandOptions: List[String], composerOptions: List[String])

object Configuration {

  /**
   * This method uses non php-openapi - so it uses reflection, is pretty ugly and is wrapped by try-catch clause
   */
  def forProject(project: Project): Either[Option[Throwable], Configuration] = {
    try {
      val service = project.getPicoContainer.getComponentInstance("com.jetbrains.php.composer.ComposerDataService")
      val askConfigurationMethod = service.getClass.getMethod("askForValidConfigurationIfNeeded")
      if(java.lang.Boolean.TRUE.equals(askConfigurationMethod.invoke(service))) {
        val (executable, commandOptions) = findExecutableAndCommandOptions(project)
        val composerOptions = ProjectSettings.getInstance(project).getComposerUpdateOptionsSettings.values

        Right(Configuration(executable, commandOptions, composerOptions))
      } else {
        Left(None)
      }
    } catch {
      case e: Throwable => Left(Some(e))
    }
  }

  private def findExecutableAndCommandOptions(project: Project): (String, List[String]) = {
    val service = project.getPicoContainer.getComponentInstance("com.jetbrains.php.composer.ComposerDataService")

    Try {
      import scala.collection.JavaConverters._
      val getComposerCommandBeginning = service.getClass.getMethod("getComposerCommandBeginning")
      val (executable :: options) = getComposerCommandBeginning.invoke(service).asInstanceOf[java.util.List[String]].asScala.toList

      (executable, options)
    }.recover {
      // fallback for versions older than 2017.3
      case _: Throwable =>
        val phpPath = getPhpPath(project, service)
        val getPathMethod = service.getClass.getMethod("getPath")
        (phpPath, List(getPathMethod.invoke(service).toString))
    }.get
  }

  private def getPhpPath(project: Project, service: AnyRef): String = {
    def getFromGlobalSettings = {
      val macroClass = Class.forName("com.jetbrains.php.macro.PhpExecutableMacro", true, service.getClass.getClassLoader)
      val method = macroClass.getMethod("getPhpCommand", classOf[Project])

      method.invoke(null, project).toString
    }

    Option(service.getClass.getMethod("getPhpPath"))
      .flatMap(method => Option(method.invoke(service)))
      .flatMap(value => Option(value.toString))
      .getOrElse(getFromGlobalSettings)
  }
}
