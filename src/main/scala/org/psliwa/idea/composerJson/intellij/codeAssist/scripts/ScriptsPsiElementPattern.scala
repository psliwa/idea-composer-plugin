package org.psliwa.idea.composerJson.intellij.codeAssist.scripts

import com.intellij.json.psi.{JsonArray, JsonProperty, JsonStringLiteral}
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.StandardPatterns.or
import org.psliwa.idea.composerJson.intellij.PsiElements.rootPsiElementPattern

object ScriptsPsiElementPattern {
  private val RootElement = psiElement(classOf[JsonProperty])
    .withName("scripts")
    .withSuperParent(2, rootPsiElementPattern)

  val Pattern = or(
    psiElement(classOf[JsonStringLiteral])
      .withParent(classOf[JsonArray])
      .withSuperParent(4, RootElement),
    psiElement()
      .afterLeaf(":")
      .withSuperParent(3, RootElement)
  )
}
