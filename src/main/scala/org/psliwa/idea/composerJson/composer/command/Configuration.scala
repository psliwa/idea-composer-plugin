package org.psliwa.idea.composerJson.composer.command

import com.intellij.openapi.project.Project

final case class Configuration(phpPath: String, composerPath: String) {
  lazy val executable = phpPath+" "+composerPath
}

object Configuration {

  /**
   * This method uses non php-openapi - so it uses reflection, is pretty ugly and is wrapped by try-catch clause
   */
  def forProject(project: Project): Either[Option[Throwable], Configuration] = {
    try {
      val service = project.getPicoContainer.getComponentInstance("com.jetbrains.php.composer.ComposerDataService")
      val askConfigurationMethod = service.getClass.getMethod("askForValidConfigurationIfNeeded")
      if(java.lang.Boolean.TRUE.equals(askConfigurationMethod.invoke(service))) {
        val phpPath = getPhpPath(project, service)
        val getPathMethod = service.getClass.getMethod("getPath")

        Right(Configuration(phpPath, getPathMethod.invoke(service).toString))
      } else {
        Left(None)
      }
    } catch {
      case e: Throwable => Left(Some(e))
    }
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
