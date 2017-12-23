package org.psliwa.idea.composerJson.intellij

import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import org.psliwa.idea.composerJson.settings.AppSettings

class NotificationsHandler(project: Project) extends ProjectComponent {
  override def projectOpened(): Unit = {
    if(!AppSettings.getInstance.wasCharityNotificationShown && AppSettings.getInstance.isCharityNotificationStillValid) {
      AppSettings.getInstance.charityNotificationWasShown()
      val title = "Dear programmer! Please read and enjoy :)"
      val text =
        """
          |<br />
          |I am glad that you use <b>PHP composer.json support</b> plugin in your daily work. I hope you like the plugin and it is helpful. I would like to take the opportunity to pass you a special message.<br /><br />
          |
          |<b>TL;DR</b>: charity is awesome, let's help others if you have opportunity. Also you can star my project on <a href="https://github.com/psliwa/idea-composer-plugin">github</a> or/and give a vote on <a href="https://plugins.jetbrains.com/plugin/7631-php-composer-json-support">jetbrains plugins page</a> in order to "force" <b>me to pay 1$ for charity</b>.<br /><br />
          |
          |<b>Long version</b>:<br />
          |I would just like to encourage you to <b>help others</b> - eg by <b>supporting charity</b>. If you do help already - it's great! For me and for the people from my country (Poland) this period of a year is special. During this period there is the biggest stream of charity support. I would like to encourage you to do the good things and help people who need help, because thanks to that the world becomes the better place for everyone.<br /><br />
          |
          |Maybe you are not able to help directly, but you can still do it <b>indirectly</b>. For every star on <a href="https://github.com/psliwa/idea-composer-plugin">github</a> or/and a vote on <a href="https://plugins.jetbrains.com/plugin/7631-php-composer-json-support">jetbrains plugins page</a> I will pay <b>1$ to WOŚP</b> (<a href="https://en.wosp.org.pl/">WOŚP</a> - charity found that tries to make healthcare in Poland more accessible for everyone, especially for newborns and seniors).<br /><br />
          |
          |I would like to state that I am waiting for the stars and the votes from <b>24th December 2017</b> to <b>14th January 2018</b> and the upper limit of my payment is <b>1000$</b> (it may be done in several tranches). To be clear, there is the state <b>before 24th December 2017</b>: stars on <a href="https://github.com/psliwa/idea-composer-plugin">github</a>: <b>69</b>, votes on <a href="https://plugins.jetbrains.com/plugin/7631-php-composer-json-support">jetbrains plugins page</a>: <b>2</b>.<br /><br />
          |
          |So you may help in two ways (you can combine!):<br /><br />
          |
          |<ul>
          |<li>Support people who need help, eg by supporting local charity founds in your country - especially that ones which spend more than 80% of income for statuary goals</li>
          |<li>Give a "star" on <a href="https://github.com/psliwa/idea-composer-plugin">github</a> or/and add a vote on <a href="https://plugins.jetbrains.com/plugin/7631-php-composer-json-support">jetbrains plugins page</a></li>
          |</ul>
          |
          |<br />
          |Thanks for reading, all the best and enjoy!<br /><br />
          |
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
