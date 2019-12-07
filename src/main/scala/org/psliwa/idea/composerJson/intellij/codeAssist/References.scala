package org.psliwa.idea.composerJson.intellij.codeAssist

private[codeAssist] object References {
  def getFixedReferenceName(s: String): String =
    s.replace(EmptyNamePlaceholder + " ", "").replace("\\\\", "\\").stripPrefix("\"").stripSuffix("\"")
}
