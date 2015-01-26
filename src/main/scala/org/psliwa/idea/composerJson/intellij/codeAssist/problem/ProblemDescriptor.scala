package org.psliwa.idea.composerJson.intellij.codeAssist.problem

import com.intellij.psi.PsiElement

private[codeAssist] case class ProblemDescriptor[QuickFix](element: PsiElement, message: String, quickFixes: Seq[QuickFix] = Seq())