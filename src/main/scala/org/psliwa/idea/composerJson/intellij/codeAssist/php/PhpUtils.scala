package org.psliwa.idea.composerJson.intellij.codeAssist.php

import com.jetbrains.php.lang.psi.elements.PhpClass

private object PhpUtils {
  def getFixedFQNamespace(phpClass: PhpClass) = escapeSlashes(phpClass.getNamespaceName.stripPrefix("\\"))

  def getFixedFQN(phpClass: PhpClass) = escapeSlashes(phpClass.getFQN.stripPrefix("\\"))

  def getFixedReferenceName(s: String) = s.replace("IntellijIdeaRulezzz ", "").replace("\\\\", "\\").stripPrefix("\"").stripSuffix("\"")

  def ensureLandingSlash(s: String) = if(s.isEmpty || s.charAt(0) != '\\') "\\"+s else s

  def escapeSlashes(s: String) = s.replace("\\", "\\\\")

  def getCallableInfo(s: String): (String, String) = {
    s.replace("::", "").splitAt(positive(s.indexOf("::"), s.size))
  }

  private def positive(i: Int, default: => Int): Int = {
    if(i >= 0) i
    else default
  }
}
