package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.{EditorNotificationPanel, EditorNotifications}
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.composer.model.repository.RepositoryProvider
import org.psliwa.idea.composerJson.intellij.codeAssist.BaseLookupElement
import org.psliwa.idea.composerJson.settings.ProjectSettings

class CustomRepositoriesEditorNotificationProvider(notifications: EditorNotifications, project: Project)
    extends EditorNotifications.Provider[EditorNotificationPanel] {
  import CustomRepositoriesEditorNotificationProvider._

  override def getKey: Key[EditorNotificationPanel] = key

  override def createNotificationPanel(file: VirtualFile, fileEditor: FileEditor): EditorNotificationPanel = {
    if (file.getName == ComposerJson && isCustomRepositoriesSupportUnspecified(file)) {
      val panel = new EditorNotificationPanel()
        .text(ComposerBundle.message("editorNotifications.customRepositories"))

      panel.createActionLabel(
        ComposerBundle.message("editorNotifications.customRepositories.yes"),
        () => {
          getSettings().enable(file.getCanonicalPath)
          notifications.updateNotifications(file)
        }
      )
      panel.createActionLabel(
        ComposerBundle.message("editorNotifications.customRepositories.no"),
        () => {
          getSettings().disable(file.getCanonicalPath)
          notifications.updateNotifications(file)
        }
      )

      panel
    } else {
      null
    }
  }

  private def getSettings(): ProjectSettings.CustomRepositoriesSettings = {
    ProjectSettings.getInstance(project).getCustomRepositoriesSettings
  }

  private def isCustomRepositoriesSupportUnspecified(file: VirtualFile): Boolean = {
    !getRepositoryProvider.hasDefaultRepository(file.getCanonicalPath) &&
    getSettings().isUnspecified(file.getCanonicalPath)
  }

  private def getRepositoryProvider: RepositoryProvider[_ <: BaseLookupElement] = {
    ApplicationManager.getApplication.getComponent(classOf[PackagesLoader]).repositoryProviderFor(project)
  }
}

private object CustomRepositoriesEditorNotificationProvider {
  val key: Key[EditorNotificationPanel] = Key.create("Custom repositories")
  val EnableAction = "enable"
  val DisableAction = "disable"
}
