package org.psliwa.idea.composerJson.intellij

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.psliwa.idea.composerJson.settings.AppSettings

class NotificationsHandler(project: Project) extends ProjectComponent {
  override def projectOpened(): Unit = {
    if (AppSettings.getInstance.wasCharityNotificationShown &&
        !AppSettings.getInstance.wasCharitySummaryNotificationShown &&
        AppSettings.getInstance.isCharityNotificationStillValid) {
      AppSettings.getInstance.charitySummaryNotificationWasShown()

      val title = "Thank you dear programmer! :)"
      val text =
        """
          |Three weeks ago I released new version of <b>PHP composer.json support</b> plugin with the special message directed to the users. In this message I wanted to encourage everyone to support charity and help others as much as you can. Also I declared myself to pay 1$ for <a href="https://en.wosp.org.pl/">WOŚP</a> for every star on <a href="https://github.com/psliwa/idea-composer-plugin">github</a> and every vote on <a href="https://plugins.jetbrains.com/plugin/7631-php-composer-json-support">jetbrains plugins page</a> between 24th December 2017 and 14th January 2018.
          |<br /><br />
          |There was <b>328</b> <a href="https://github.com/psliwa/idea-composer-plugin">github</a> stars and <a href="https://plugins.jetbrains.com/plugin/7631-php-composer-json-support">plugin</a> votes in total in this time frame. Additionally I was given <b>5$</b> via paypal, so my donation to <a href="https://en.wosp.org.pl/">WOŚP</a> will be at least <b>333$</b>.
          |<br /><br />
          |Up to two weeks I will publish payments confirmation on my <a href="https://twitter.com/psliwa">twitter</a> account.
          |<br /><br />
          |Thank you for participation, enjoy!
          |<br /><br />
          |@psliwa
        """.stripMargin
      Notifications.balloonInfo(title, text, Some(project))
    }
  }

  override def initComponent(): Unit = {}
  override def disposeComponent(): Unit = {}
  override def getComponentName: String = "NotificationsHandler"
  override def projectClosed(): Unit = {}
}
