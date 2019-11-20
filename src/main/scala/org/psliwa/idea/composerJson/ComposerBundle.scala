package org.psliwa.idea.composerJson

import java.util.ResourceBundle

import com.intellij.CommonBundle
import org.jetbrains.annotations.PropertyKey

object ComposerBundle {
  final private val BundleName = "org.psliwa.idea.composerJson.messages.ComposerBundle"
  private val Bundle = ResourceBundle.getBundle(BundleName)

  def message(@PropertyKey(resourceBundle = BundleName) key: String, params: AnyRef*): String = {
    CommonBundle.message(Bundle, key, params: _*)
  }

  def message(@PropertyKey(resourceBundle = BundleName) key: String): String = message(key, Nil: _*)
}
