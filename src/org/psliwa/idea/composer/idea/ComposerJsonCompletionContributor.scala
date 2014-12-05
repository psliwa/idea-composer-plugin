package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion._
import com.intellij.json.JsonLanguage
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext

class ComposerJsonCompletionContributor extends CompletionContributor {
  extend(
    CompletionType.BASIC,
    PlatformPatterns.psiElement(classOf[PsiElement]).withLanguage(JsonLanguage.INSTANCE),
    new CompletionProvider[CompletionParameters]() {
      override def addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet): Unit = {
        if(parameters.getOriginalFile.getName == "composer.json") {
          //TODO
        }
      }
    }
  )
}
