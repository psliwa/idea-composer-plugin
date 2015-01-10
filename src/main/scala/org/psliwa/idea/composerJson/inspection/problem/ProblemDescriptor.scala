package org.psliwa.idea.composerJson.inspection.problem

import com.intellij.psi.PsiElement

private[inspection] case class ProblemDescriptor[QuickFix](element: PsiElement, message: String, quickFixes: Seq[QuickFix] = Seq())