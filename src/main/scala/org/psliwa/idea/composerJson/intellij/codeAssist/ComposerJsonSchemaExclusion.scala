package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.remote.JsonSchemaCatalogExclusion
import org.psliwa.idea.composerJson.ComposerJson

class ComposerJsonSchemaExclusion extends JsonSchemaCatalogExclusion {
  override def isExcluded(file: VirtualFile): Boolean = file.getName == ComposerJson
}
