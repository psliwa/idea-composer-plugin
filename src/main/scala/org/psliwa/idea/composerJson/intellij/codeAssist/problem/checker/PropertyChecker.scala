package org.psliwa.idea.composerJson.intellij.codeAssist.problem.checker

import com.intellij.json.psi.JsonObject
import org.psliwa.idea.composerJson.intellij.codeAssist.problem._
import scala.util.matching.Regex
import PropertyPath._

private[codeAssist] case class PropertyChecker(propertyPath: PropertyPath, condition: Condition = ConditionExists) extends Checker {
  def is(value: Any) = copy(condition = ConditionIs(value))
  def isNot(value: Any) = copy(condition = ConditionIsNot(value))
  def matches(regex: Regex) = copy(condition = ConditionMatch(regex))
  def duplicatesSibling(siblingPropertyName: String) = {
    MultiplePropertiesChecker(propertyPath, ConditionDuplicateIn(siblingPropertyPath(propertyPath, siblingPropertyName)))
  }

  override def check(jsonObject: JsonObject): CheckResult = condition.check(jsonObject, propertyPath)
}
