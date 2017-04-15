package org.psliwa.idea.composerJson.intellij.filetype

import com.intellij.openapi.fileTypes.{ExactFileNameMatcher, FileTypeConsumer, FileTypeFactory}
import org.psliwa.idea.composerJson.ComposerJson
import org.psliwa.idea.composerJson.ComposerLock

class ComposerJsonFileTypeFactory extends FileTypeFactory {
  override def createFileTypes(consumer: FileTypeConsumer): Unit = {
    consumer.consume(ComposerJsonFileType.INSTANCE, new ExactFileNameMatcher(ComposerJson))
    consumer.consume(ComposerJsonFileType.INSTANCE, new ExactFileNameMatcher(ComposerLock))
  }
}
