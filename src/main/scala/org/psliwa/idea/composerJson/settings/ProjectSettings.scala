package org.psliwa.idea.composerJson.settings

import java.util

import com.intellij.openapi.components._
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.NotNull

import scala.collection.mutable
import org.jdom.Element
import org.psliwa.idea.composerJson.settings

@State(
  name = "ComposerJsonPluginSettings",
  storages = Array(
    new Storage("/composerJson.xml")
  )
)
class ProjectSettings extends PersistentStateComponent[Element] {
  private val unboundedVersionInspectionSettings: ProjectSettings.UnboundedVersionInspectionSettings = new ProjectSettings.UnboundedVersionInspectionSettings
  private val customRepositoriesSettings = new ProjectSettings.CustomRepositoriesSettings()
  private val composerUpdateOptions = new settings.ProjectSettings.ComposerUpdateOptions()

  def getState: Element = {
    val element = new Element("ComposerJsonSettings")
    writeUnboundedVersionsInspectionState(element)
    writeCustomRepositoriesState(element)
    writeComposerUpdateOptionsState(element)

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

  private def writeCustomRepositoriesState(element: Element): Unit = {
    val customRepositoriesElement = new Element("customRepositories")
    element.addContent(customRepositoriesElement)

    val customRepositories = customRepositoriesSettings.getConfigurationForScala
    customRepositories
      .map{ case(file, enabled) => new Element("file").setAttribute("path", file).setAttribute("enabled", enabled.toString)}
      .foreach(customRepositoriesElement.addContent)
  }

  private def writeComposerUpdateOptionsState(element: Element): Unit = {
    import scala.collection.JavaConverters._

    val composerUpdateOptionsElement = new Element("composerUpdateOptions")
    element.addContent(composerUpdateOptionsElement)

    composerUpdateOptions.getValues().asScala
      .map(textItem => new Element("option").setAttribute("name", textItem.getText))
      .foreach(composerUpdateOptionsElement.addContent)
  }

  def loadState(state: Element) {
    loadUnboundedVersionsInspectionState(state)
    loadCustomRepositoriesState(state)
    loadComposerUpdateOptionsState(state)
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

    if(patterns.isEmpty) {
      // default value
      unboundedVersionInspectionSettings.addExcludedPattern(new PatternItem("roave/security-advisories"))
    } else {
      unboundedVersionInspectionSettings.clear()
      patterns.foreach(unboundedVersionInspectionSettings.addExcludedPattern)
    }
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

  private def loadComposerUpdateOptionsState(state: Element): Unit = {
    import scala.collection.JavaConverters._

    val options: Seq[String] = for {
      composerUpdateOptions <- state.getChildren("composerUpdateOptions").asScala
      option <- composerUpdateOptions.getChildren("option").asScala
      optionValueAttr <- Option(option.getAttribute("name")).toList
      optionValue <- Option(optionValueAttr.getValue).toList
    } yield optionValue

    options.foreach(composerUpdateOptions.addOption)
  }

  def getUnboundedVersionInspectionSettings: ProjectSettings.UnboundedVersionInspectionSettings = {
    unboundedVersionInspectionSettings
  }

  def getCustomRepositoriesSettings: ProjectSettings.CustomRepositoriesSettings = customRepositoriesSettings

  def getComposerUpdateOptionsSettings: ProjectSettings.ComposerUpdateOptions = {
    composerUpdateOptions
  }
}

object ProjectSettings {
  import scala.collection.JavaConverters._

  def getInstance(project: Project): ProjectSettings = ServiceManager.getService(project, classOf[ProjectSettings])
  def apply(project: Project): ProjectSettings = getInstance(project)

  class UnboundedVersionInspectionSettings private[ProjectSettings]() extends TabularSettings[PatternItem] {
    private val excludedPatterns: mutable.MutableList[PatternItem] = mutable.MutableList()

    def isExcluded(s: String): Boolean = excludedPatterns.exists(_.matches(s))

    override def getValues(): java.util.List[PatternItem] = {
      excludedPatterns.map(_.clone).distinct.asJava
    }

    override def setValues(@NotNull patterns: java.util.List[PatternItem]) {
      excludedPatterns.clear()
      patterns.asScala.map(_.clone).distinct.foreach(excludedPatterns.+=)
    }

    def addExcludedPattern(@NotNull pattern: PatternItem) {
      excludedPatterns += pattern.clone
    }

    private[settings] def getConfigurationForScala = excludedPatterns

    def clear(): Unit = excludedPatterns.clear()
  }

  class CustomRepositoriesSettings private[ProjectSettings]() extends TabularSettings[EnabledItem] {
    private val customRepositories: mutable.Map[String, Boolean] = mutable.Map()

    def isUnspecified(file: String): Boolean = !customRepositories.contains(file)
    def isEnabled(file: String): Boolean = customRepositories.getOrElse(file, false)

    override def setValues(config: java.util.List[EnabledItem]): Unit = {
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

  class ComposerUpdateOptions private[ProjectSettings]() extends TabularSettings[TextItem] {
    private val items: mutable.Set[TextItem] = mutable.Set()

    override def getValues(): util.List[TextItem] = {
      items.map(_.clone).toList.asJava
    }

    def values: List[String] = items.toList.map(_.getText)

    override def setValues(values: util.List[TextItem]): Unit = {
      items.clear()
      values.asScala.map(_.clone).foreach(items.add)
    }

    def addOption(option: String): Unit = {
      items.add(new TextItem(option))
    }

    def clear(): Unit = items.clear()
  }
}
