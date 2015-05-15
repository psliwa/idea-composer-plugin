package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.json.JsonLanguage
import com.intellij.json.psi.{JsonObject, JsonProperty, JsonFile, JsonStringLiteral}
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.StandardPatterns._
import org.psliwa.idea.composerJson._

package object composer {
  private[composer] def packageElement = {
    psiElement(classOf[JsonStringLiteral])
      .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
      .withLanguage(JsonLanguage.INSTANCE)
      .withParent(
        psiElement(classOf[JsonProperty]).withParent(
          psiElement(classOf[JsonObject]).withParent(
            or(
              psiElement(classOf[JsonProperty]).withName("require"),
              psiElement(classOf[JsonProperty]).withName("require-dev")
            )
          )
        )
      )
  }
}
