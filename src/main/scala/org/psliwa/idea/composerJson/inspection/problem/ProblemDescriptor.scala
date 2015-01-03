package org.psliwa.idea.composerJson.inspection.problem

import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.psi.PsiElement

private[inspection] case class ProblemDescriptor(element: PsiElement, message: String, quickFixes: List[LocalQuickFixOnPsiElement])