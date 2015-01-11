package org.psliwa.idea.composerJson.intellij

import com.intellij.notification
import com.intellij.notification.{NotificationGroup, NotificationType}
import com.intellij.openapi.project.Project
import org.psliwa.idea.composerJson.ComposerBundle

object Notifications {

  private val group = NotificationGroup.logOnlyGroup(ComposerBundle.message("notifications.group"))

  def info(title: String, message: String, project: Option[Project] = None) = {
    notify(title, message, project)
  }

  def error(title: String, message: String, project: Option[Project] = None) = {
    notify(title, message, project, NotificationType.ERROR)
  }

  private def notify(title: String, message: String, project: Option[Project] = None, notificationType: NotificationType = NotificationType.INFORMATION) = {
    notification.Notifications.Bus.notify(
      group.createNotification(title, message, notificationType, null),
      project.orNull
    )
  }
}
