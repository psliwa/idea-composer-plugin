package org.psliwa.idea.composerJson.settings

trait TabularSettings[A] {
  def getValues(): java.util.List[A]
  def setValues(values: java.util.List[A])
}
