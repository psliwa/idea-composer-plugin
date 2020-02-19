package org.psliwa.idea.composerJson.intellij

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.psliwa.idea.composerJson.settings.AppSettings

class NotificationsHandler(project: Project) extends ProjectComponent {
  override def projectOpened(): Unit = {
    if (!AppSettings.getInstance.wasFarewellNotificationShown) {
      AppSettings.getInstance.farewellNotificationWasShown()

      val title = "Farewell composer.json plugin users!"
      val text =
        """
          |More than 5 years ago I published first version of composer.json plugin. I created this plugin because composer.json smart editing features were missing in PhpStorm. The plugin was evolving through the time. There even was a charity action 2 years ago that was a success. After this action this plugin became the top rated - but the time has come.
          |<br /><br />
          |Since PhpStorm 2020.1 big part of the plugin's features have been built-in into IDE itself so all composer.json editing features are handled by PhpStorm itself. The plugin is not needed anymore - it is the last release.
          |<br /><br />
          |Thanks for using composer.json plugin, sending suggestions, reporting bugs and feature requests.
          |<br /><br />
          |If you wish you can <a href="https://www.paypal.me/psliwa">donate me on paypal</a>.
          |<br /><br />
          |@psliwa
          |""".stripMargin
      Notifications.balloonInfo(title, text, Some(project))
    }
  }

  override def initComponent(): Unit = {}
  override def disposeComponent(): Unit = {}
  override def getComponentName: String = "NotificationsHandler"
  override def projectClosed(): Unit = {}
}
