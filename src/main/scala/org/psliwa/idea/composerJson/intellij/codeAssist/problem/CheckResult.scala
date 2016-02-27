package org.psliwa.idea.composerJson.intellij.codeAssist.problem

private[codeAssist] case class CheckResult(value: Boolean, properties: Set[PropertyPath]) {
  def not = CheckResult(!value, properties)

  def &&(result: CheckResult) = CheckResult(value && result.value, properties ++ result.properties)

  def ||(result: CheckResult) = CheckResult(value || result.value, properties ++ result.properties)
}
