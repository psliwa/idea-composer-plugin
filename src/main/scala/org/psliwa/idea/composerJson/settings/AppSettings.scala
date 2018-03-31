package org.psliwa.idea.composerJson.settings

import java.time.{LocalDate, Month}

import com.intellij.openapi.components.{PersistentStateComponent, ServiceManager, State, Storage}
import org.jdom.Element

@State(name = "ComposerJsonPluginAppSettings", storages = Array(new Storage("$APP_CONFIG$/composerJson.xml")))
class AppSettings extends PersistentStateComponent[Element] {
  private var charityNotificationShown: Boolean = false
  private var charitySummaryNotificationShown: Boolean = false

  override def loadState(name: Element): Unit = {
    charityNotificationShown = loadBoolean(name, "charityNotificationShown")
    charitySummaryNotificationShown = loadBoolean(name, "charitySummaryNotificationShown")
  }

  private def loadBoolean(element: Element, name: String) = {
    Option(element.getChild(name))
      .exists(child => child.getValue match {
        case "true" => true
        case _ => false
      })
  }

  override def getState: Element = {
    val element = new Element("ComposerJsonPluginAppSettings")
    element.addContent(new Element("charityNotificationShown").addContent(charityNotificationShown.toString))
    element.addContent(new Element("charitySummaryNotificationShown").addContent(charitySummaryNotificationShown.toString))
  }

  def wasCharityNotificationShown: Boolean = charityNotificationShown
  def isCharityNotificationStillValid: Boolean = LocalDate.now().isBefore(LocalDate.of(2018, Month.JANUARY.getValue, 17))

  def wasCharitySummaryNotificationShown: Boolean = charitySummaryNotificationShown
  def charitySummaryNotificationWasShown(): Unit = charitySummaryNotificationShown = true
}

object AppSettings {
  def getInstance: AppSettings = ServiceManager.getService(classOf[AppSettings])
}
