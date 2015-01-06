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

  def getState: Element = {
    val element = new Element("ComposerJsonSettings")
    val unboundedVersionsInspectionSettingsElement = new Element("unboundedVersionInspectionSettings")
    val excludedPackages = new Element("excludedPackages")

    unboundedVersionsInspectionSettingsElement.addContent(excludedPackages)
    element.addContent(unboundedVersionsInspectionSettingsElement)

    val excludedPatterns = unboundedVersionInspectionSettings.getExcludedPatterns

    excludedPatterns
      .map(pattern => new Element("pattern").setAttribute("pattern", pattern.getPattern))
      .foreach(excludedPackages.addContent)

    element
  }

  def loadState(state: Element) {
    import scala.collection.JavaConversions._

    val patterns: Seq[PatternItem] = for {
      unboundedVersionsSettings <- state.getChildren("unboundedVersionInspectionSettings")
      excludedPackages <- unboundedVersionsSettings.getChildren("excludedPackages")
      pattern <- excludedPackages.getChildren("pattern")
      patternAttr <- Option(pattern.getAttribute("pattern")).toList
      patternValue <- Option(patternAttr.getValue).toList
    } yield new PatternItem(patternValue)

    patterns.foreach(unboundedVersionInspectionSettings.addExcludedPattern)
  }

  def getUnboundedVersionInspectionSettings: ComposerJsonSettings.UnboundedVersionInspectionSettings = {
    unboundedVersionInspectionSettings
  }
}

object ComposerJsonSettings {
  def getInstance(project: Project): ComposerJsonSettings = ServiceManager.getService(project, classOf[ComposerJsonSettings])
  def apply(project: Project): ComposerJsonSettings = getInstance(project)

  class UnboundedVersionInspectionSettings private[ComposerJsonSettings]() {
    private val excludedPatterns: mutable.Set[PatternItem] = mutable.Set()

    def getExcludedPatterns: Array[PatternItem] = excludedPatterns.map(_.clone).toArray

    def setExcludedPatterns(@NotNull patterns: Array[PatternItem]) {
      excludedPatterns.clear()
      patterns.map(_.clone).foreach(excludedPatterns.add)
    }

    def addExcludedPattern(@NotNull pattern: PatternItem) {
      excludedPatterns.add(pattern.clone)
    }
  }
}
