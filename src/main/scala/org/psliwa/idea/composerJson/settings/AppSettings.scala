package org.psliwa.idea.composerJson.settings

import java.time.{LocalDate, Month}

import com.intellij.openapi.components.{PersistentStateComponent, ServiceManager, State, Storage}
import org.jdom.Element

@State(name = "ComposerJsonPluginAppSettings", storages = Array(new Storage(id = "other", file = "$APP_CONFIG$/composerJson.xml")))
class AppSettings extends PersistentStateComponent[Element] {
  private var charityNotificationShown: Boolean = false

  override def loadState(t: Element): Unit = {
    charityNotificationShown = Option(t.getChild("charityNotificationShown"))
      .exists(child => child.getValue match {
        case "true" => true
        case _ => false
      })
  }

  override def getState: Element = {
    val element = new Element("ComposerJsonPluginAppSettings")
    element.addContent(new Element("charityNotificationShown").addContent(charityNotificationShown.toString))
  }

  def wasCharityNotificationShown: Boolean = charityNotificationShown
  def isCharityNotificationStillValid: Boolean = LocalDate.now().isBefore(LocalDate.of(2018, Month.JANUARY.getValue, 17))
  def charityNotificationWasShown(): Unit = charityNotificationShown = true
}

object AppSettings {
  def getInstance: AppSettings = ServiceManager.getService(classOf[AppSettings])
}
