package org.psliwa.idea.composerJson.intellij.filetype

import javax.swing.Icon

import com.intellij.json.JsonLanguage
import com.intellij.openapi.fileTypes.LanguageFileType
import org.psliwa.idea.composerJson.Icons

class ComposerJsonFileType extends LanguageFileType(JsonLanguage.INSTANCE) {
  override def getName: String = "composer.json"

  override def getDescription: String = "Composer configuration file"

  override def getIcon: Icon = Icons.Composer

  override def getDefaultExtension: String = "json"
}

object ComposerJsonFileType {
  val INSTANCE = new ComposerJsonFileType
}
