package org.psliwa.idea.composerJson.settings

import com.intellij.openapi.components._
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NotNull
import scala.collection.mutable
import org.jdom.Element

@State(
  name = "ComposerJsonPluginSettings",
  storages = Array(
    new Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
    new Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/composerJson.xml", scheme = StorageScheme.DIRECTORY_BASED)
  )
)
class ComposerJsonSettings extends PersistentStateComponent[Element] {
  private val unboundedVersionInspectionSettings: ComposerJsonSettings.UnboundedVersionInspectionSettings = new ComposerJsonSettings.UnboundedVersionInspectionSettings
  private val customRepositoriesSettings = new ComposerJsonSettings.CustomRepositoriesSettings()

  def getState: Element = {
    val element = new Element("ComposerJsonSettings")
    writeUnboundedVersionsInspectionState(element)
    writeCustomRepositoriesState(element)

    element
  }

  private def writeUnboundedVersionsInspectionState(element: Element): Unit = {
    val unboundedVersionsInspectionSettingsElement = new Element("unboundedVersionInspectionSettings")
    val excludedPackages = new Element("excludedPackages")

    unboundedVersionsInspectionSettingsElement.addContent(excludedPackages)
    element.addContent(unboundedVersionsInspectionSettingsElement)

    val excludedPatterns = unboundedVersionInspectionSettings.getConfigurationForScala

    excludedPatterns
      .map(pattern => new Element("pattern").setAttribute("pattern", pattern.getPattern))
      .foreach(excludedPackages.addContent)
  }

  private def writeCustomRepositoriesState(element: Element) = {
    val customRepositoriesElement = new Element("customRepositories")
    element.addContent(customRepositoriesElement)

    val customRepositories = customRepositoriesSettings.getConfigurationForScala
    customRepositories
      .map{ case(file, enabled) => new Element("file").setAttribute("path", file).setAttribute("enabled", enabled.toString)}
      .foreach(customRepositoriesElement.addContent)
  }

  def loadState(state: Element) {
    loadUnboundedVersionsInspectionState(state)
    loadCustomRepositoriesState(state)
  }

  private def loadUnboundedVersionsInspectionState(state: Element): Unit = {
    import scala.collection.JavaConverters._

    val patterns: Seq[PatternItem] = for {
      unboundedVersionsSettings <- state.getChildren("unboundedVersionInspectionSettings").asScala
      excludedPackages <- unboundedVersionsSettings.getChildren("excludedPackages").asScala
      pattern <- excludedPackages.getChildren("pattern").asScala
      patternAttr <- Option(pattern.getAttribute("pattern")).toList
      patternValue <- Option(patternAttr.getValue).toList
    } yield new PatternItem(patternValue)

    patterns.foreach(unboundedVersionInspectionSettings.addExcludedPattern)
  }

  private def loadCustomRepositoriesState(state: Element): Unit = {
    import scala.collection.JavaConverters._

    val config: Seq[(String,Boolean)] = for {
      customRepositoriesSettings <- state.getChildren("customRepositories").asScala
      file <- customRepositoriesSettings.getChildren("file").asScala
      pathAttr <- Option(file.getAttribute("path")).toList
      path <- Option(pathAttr.getValue).toList
      enabledAttr <- Option(file.getAttribute("enabled")).toList
      rawEnabled <- Option(enabledAttr.getValue).toList
      enabled = rawEnabled == "true"
    } yield path -> enabled

    config.foreach{ case(file, enabled) => customRepositoriesSettings.setConfigurationForFile(file, enabled)}
  }

  def getUnboundedVersionInspectionSettings: ComposerJsonSettings.UnboundedVersionInspectionSettings = {
    unboundedVersionInspectionSettings
  }

  def getCustomRepositoriesSettings = customRepositoriesSettings
}

object ComposerJsonSettings {
  import scala.collection.JavaConverters._

  def getInstance(project: Project): ComposerJsonSettings = ServiceManager.getService(project, classOf[ComposerJsonSettings])
  def apply(project: Project): ComposerJsonSettings = getInstance(project)

  class UnboundedVersionInspectionSettings private[ComposerJsonSettings]() extends TabularSettings[PatternItem] {
    private val excludedPatterns: mutable.Set[PatternItem] = mutable.Set()

    def isExcluded(s: String): Boolean = excludedPatterns.exists(_.matches(s))

    override def getValues(): java.util.List[PatternItem] = {
      excludedPatterns.map(_.clone).toList.asJava
    }

    override def setValues(@NotNull patterns: java.util.List[PatternItem]) {
      excludedPatterns.clear()
      patterns.asScala.map(_.clone).foreach(excludedPatterns.add)
    }

    def addExcludedPattern(@NotNull pattern: PatternItem) {
      excludedPatterns.add(pattern.clone)
    }

    private[settings] def getConfigurationForScala = excludedPatterns

    def clear(): Unit = excludedPatterns.clear()
  }

  class CustomRepositoriesSettings private[ComposerJsonSettings]() extends TabularSettings[EnabledItem] {
    private val customRepositories: mutable.Map[String, Boolean] = mutable.Map()

    def isUnspecified(file: String) = !customRepositories.contains(file)
    def isEnabled(file: String) = customRepositories.getOrElse(file, false)

    override def setValues(config: java.util.List[EnabledItem]) = {
      customRepositories.clear()
      for(item <- config.asScala) {
        customRepositories(item.getName) = item.isEnabled
      }
    }

    override def getValues(): java.util.List[EnabledItem] = {
      customRepositories
        .map{ case(file, enabled) => new EnabledItem(file, enabled) }
        .toList
        .asJava
    }

    def setConfigurationForFile(file: String, enabled: Boolean): Unit = {
      customRepositories(file) = enabled
    }

    def enable(file: String): Unit = customRepositories(file) = true
    def disable(file: String): Unit = customRepositories(file) = false

    private[settings] def getConfigurationForScala = customRepositories

    def clear(): Unit = customRepositories.clear()
  }
}
