package org.psliwa.idea.composerJson.intellij.codeAssist.problem.checker

import com.intellij.json.psi.JsonObject
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.PropertyPath

import scala.language.implicitConversions

private[codeAssist] trait Checker {
  def check(jsonObject: JsonObject): CheckResult

  def &&(checker: Checker): Checker = AndChecker(this, checker)
  def ||(checker: Checker): Checker = OrChecker(this, checker)
}

private[codeAssist] case class CheckResult(value: Boolean, properties: Set[PropertyPath]) {
  lazy val not = CheckResult(!value, properties)

  def &&(result: CheckResult) = CheckResult(value && result.value, properties ++ result.properties)

  def ||(result: CheckResult) = CheckResult(value || result.value, properties ++ result.properties)
}

private[codeAssist] object Checker {
  def not(checker: Checker) = new Checker {
    override def check(jsonObject: JsonObject): CheckResult = checker.check(jsonObject).not
  }
}

private[codeAssist] object ImplicitConversions {
  implicit def stringToProblemChecker(property: String): PropertyChecker = PropertyChecker(property)
  implicit def propertyPathToProblemChecker(propertyPath: PropertyPath): PropertyChecker = PropertyChecker(propertyPath)
  implicit def stringToPropertyPath(property: String): PropertyPath = PropertyPath(property, List.empty)
}

private[codeAssist] case class AndChecker(checker1: Checker, checker2: Checker) extends Checker{
  override def check(jsonObject: JsonObject): CheckResult = checker1.check(jsonObject) && checker2.check(jsonObject)
}

private[codeAssist] case class OrChecker(checker1: Checker, checker2: Checker) extends Checker{
  override def check(jsonObject: JsonObject): CheckResult = checker1.check(jsonObject) || checker2.check(jsonObject)
}