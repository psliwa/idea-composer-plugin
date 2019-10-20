package org.psliwa.idea.composerJson.composer.command

import com.intellij.execution.ExecutionException
import com.intellij.execution.process.{ProcessAdapter, ProcessEvent}
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.progress.{PerformInBackgroundOption, ProgressIndicator, ProgressManager}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import com.jetbrains.php.composer.ComposerDataService
import com.jetbrains.php.composer.execution.ComposerExecution
import org.psliwa.idea.composerJson
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.composer.ComposerPackage
import org.psliwa.idea.composerJson.composer.command.DefaultPackagesInstaller.Result
import org.psliwa.idea.composerJson.intellij.Notifications

import scala.compat.Platform.EOL

trait PackagesInstaller {
  def install(packages: List[ComposerPackage]): Unit
}

class DefaultPackagesInstaller(project: Project, file: PsiFile) extends PackagesInstaller {
  override def install(packages: List[ComposerPackage]): Unit = {
    try {
      installInBackground(packages)
    } catch {
      case e: Throwable =>
        // there is used non open php api, so many runtime exceptions may be thrown
        handleFailure(packages, Result.Failure(e).message)
    }
  }

  private def installInBackground(packages: List[ComposerPackage]): Unit = {
    val service = project.getComponent(classOf[ComposerDataService])
    if (service.askForValidConfigurationIfNeeded()) {
      val composerExecution = service.getComposerExecution

      val task: Backgroundable = new Backgroundable(project, ComposerBundle.message("inspection.notInstalledPackage.installing"), true, PerformInBackgroundOption.DEAF) {
        override def run(indicator: ProgressIndicator): Unit = {
          val packageNames = packages.map(_.name).mkString(", ")

          install(indicator) match {
            case Result.Failure(message) => {
              handleFailure(packages, message)
            }
            case Result.Success => {
              //refresh parent directory and composer.lock file in order to inter alia reanalyze composer.json
              val parentDir = file.getVirtualFile.getParent
              parentDir.refresh(true, false)
              Option(parentDir.findChild("vendor")).foreach(_.refresh(true, true))
              Option(parentDir.findChild(composerJson.ComposerLock)).foreach(_.refresh(true, false))

              Notifications.info(
                ComposerBundle.message("inspection.notInstalledPackage.successTitle"),
                ComposerBundle.message("inspection.notInstalledPackage.success", packageNames),
                Some(project)
              )
            }
            case Result.Cancelled =>
              Notifications.info(
                ComposerBundle.message("inspection.notInstalledPackage.cancelledTitle"),
                ComposerBundle.message("inspection.notInstalledPackage.cancelled", packageNames),
                Some(project))
          }
        }

        private def install(indicator: ProgressIndicator): Result = {
          def installInBackground(composerExecution: ComposerExecution): Result = {
            indicator.setIndeterminate(true)

            val packagesParams = packages.map(pkg => pkg.name)
            indicator.setText(s"installing packages: ${packagesParams.mkString(", ")}")

            val message = new StringBuilder()

            try {
              import scala.collection.JavaConverters._
              val handler = composerExecution
                .createProcessHandler(project, file.getVirtualFile.getParent.getCanonicalPath, ("update" :: packages.map(_.name)).asJava, "")

              handler.addProcessListener(new ProcessAdapter {
                override def onTextAvailable(event: ProcessEvent, outputType: Key[_]): Unit = {
                  indicator.setText2(event.getText)
                  message
                    .append("\n")
                    .append(event.getText)
                }
              })

              handler.startNotify()

              var finished = false
              while (!finished) {
                finished = handler.waitFor(1000L)

                if (indicator.isCanceled) {
                  composerExecution.cancelProcess(handler)
                  finished = true
                }
              }

              if (!indicator.isCanceled && handler.getExitCode != 0) {
                Result.Failure(message.toString())
              } else if (indicator.isCanceled) {
                Result.Cancelled
              } else {
                Result.Success
              }
            } catch {
              case e: ExecutionException =>
                Result.Failure(e)
            }
          }

          try {
            installInBackground(composerExecution)
          } catch {
            case e: Throwable =>
              // there is used non open php api, so many runtime exceptions may be thrown
              Result.Failure(e)
          }
        }
      }

      ProgressManager.getInstance().run(task)
    }
  }

  private def handleFailure(packages: List[ComposerPackage], message: String): Unit = {
    val installationFailed = ComposerBundle.message(
      "inspection.notInstalledPackage.errorTitle",
      packages.map(pkg => pkg.name + " (" + pkg.version + ")").mkString(", ")
    )

    Notifications.error(installationFailed, message, Some(project))
  }
}

private object DefaultPackagesInstaller {
  sealed trait Result
  object Result {
    case class Failure(message: String) extends Result
    object Failure {
      def apply(e: Throwable): Failure = Failure(e.toString+"\n\n"+e.getStackTrace.mkString("", EOL, EOL))
    }
    case object Success extends Result
    case object Cancelled extends Result
  }
}
