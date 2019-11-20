package org.psliwa.idea.composerJson.intellij

import com.intellij.notification
import com.intellij.notification._
import com.intellij.openapi.project.Project
import org.psliwa.idea.composerJson.ComposerBundle

object Notifications {

  private val logOnlyGroup = NotificationGroup.logOnlyGroup(ComposerBundle.message("notifications.group.log"))
  private val balloonGroup = new NotificationGroup(ComposerBundle.message("notifications.group.balloon"),
                                                   NotificationDisplayType.TOOL_WINDOW,
                                                   true)

  def info(title: String, message: String, project: Option[Project] = None): Unit = {
    notify(title, message, project)
  }

  def balloonInfo(title: String, message: String, project: Option[Project] = None): Unit = {
    notify(title, message, project, notificationGroup = balloonGroup)
  }

  def error(title: String, message: String, project: Option[Project] = None): Unit = {
    notify(title, message, project, NotificationType.ERROR)
  }

  private def notify(title: String,
                     message: String,
                     project: Option[Project] = None,
                     notificationType: NotificationType = NotificationType.INFORMATION,
                     notificationGroup: NotificationGroup = logOnlyGroup): Unit = {
    notification.Notifications.Bus.notify(
      notificationGroup.createNotification(title,
                                           message,
                                           notificationType,
                                           new NotificationListener.UrlOpeningListener(false)),
      project.orNull
    )
  }
}
