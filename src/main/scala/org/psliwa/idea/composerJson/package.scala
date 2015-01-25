package org.psliwa.idea

import org.psliwa.idea.composerJson.json.{Schema, SchemaLoader}

package object composerJson {
  val ComposerJson = "composer.json"
  val ComposerLock = "composer.lock"
  val ComposerSchemaFilepath = "/org/psliwa/idea/composerJson/composer-schema.json"
  val EmptyPsiElementNamePlaceholder = "IntellijIdeaRulezzz"
  lazy val ComposerSchema: Option[Schema] = SchemaLoader.load(ComposerSchemaFilepath)
}