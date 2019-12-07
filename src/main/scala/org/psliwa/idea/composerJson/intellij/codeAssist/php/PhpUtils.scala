package org.psliwa.idea.composerJson.intellij.codeAssist.php

import com.jetbrains.php.lang.psi.elements.PhpClass
import org.psliwa.idea.composerJson.intellij.codeAssist.References

private object PhpUtils {
  def getFixedFQNamespace(phpClass: PhpClass): String = escapeSlashes(phpClass.getNamespaceName.stripPrefix("\\"))

  def getFixedFQN(phpClass: PhpClass): String = escapeSlashes(phpClass.getFQN.stripPrefix("\\"))

  def getFixedReferenceName(s: String): String = References.getFixedReferenceName(s)

  def ensureLandingSlash(s: String): String = if (s.isEmpty || s.charAt(0) != '\\') "\\" + s else s

  def escapeSlashes(s: String): String = s.replace("\\", "\\\\")

  def getCallableInfo(s: String): (String, String) = {
    s.replace("::", "").splitAt(positive(s.indexOf("::"), s.length))
  }

  private def positive(i: Int, default: => Int): Int = {
    if (i >= 0) i
    else default
  }
}
