package org.psliwa.idea.composerJson.reference.php

import com.jetbrains.php.lang.psi.elements.PhpClass

private object PhpUtils {
  def getFixedFQNamespace(phpClass: PhpClass) = escapeSlashes(phpClass.getNamespaceName.stripPrefix("\\"))

  def getFixedFQN(phpClass: PhpClass) = escapeSlashes(phpClass.getFQN.stripPrefix("\\"))

  private def escapeSlashes(s: String) = s.replace("\\", "\\\\")
}
