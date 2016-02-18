package org.psliwa.idea.composerJson.intellij.codeAssist.problem

import com.intellij.codeInspection.{ProblemHighlightType, LocalQuickFixOnPsiElement}
import com.intellij.json.psi.JsonObject

import scala.language.implicitConversions
import scala.util.matching.Regex

private[codeAssist] case class ProblemChecker(
  checker: Checker,
  properties: List[String],
  problem: String,
  createQuickFixes: JsonObject => List[LocalQuickFixOnPsiElement],
  highlightType: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING
) extends Checker {
  override def check(jsonObject: JsonObject): Boolean = checker.check(jsonObject)
}

private[codeAssist] trait Checker {
  def check(jsonObject: JsonObject): Boolean

  def &&(checker: Checker): Checker = AndChecker(this, checker)
  def ||(checker: Checker): Checker = OrChecker(this, checker)
}

private[codeAssist] case class PropertyChecker(property: String, condition: Condition = ConditionExists) extends Checker {
  def is(value: Any) = copy(condition = ConditionIs(value))
  def isNot(value: Any) = copy(condition = ConditionIsNot(value))
  def matches(regex: Regex) = copy(condition = ConditionMatch(regex))

  override def check(jsonObject: JsonObject): Boolean = condition.check(jsonObject, property)
}

private[codeAssist] object Checker {
  def not(checker: Checker) = new Checker {
    override def check(jsonObject: JsonObject): Boolean = !checker.check(jsonObject)
  }
}

private[codeAssist] object ImplicitConversions {
  implicit def stringToProblemChecker(property: String): PropertyChecker = PropertyChecker(property)
}

private[codeAssist] case class AndChecker(checker1: Checker, checker2: Checker) extends Checker{
  override def check(jsonObject: JsonObject): Boolean = checker1.check(jsonObject) && checker2.check(jsonObject)
}

private[codeAssist] case class OrChecker(checker1: Checker, checker2: Checker) extends Checker{
  override def check(jsonObject: JsonObject): Boolean = checker1.check(jsonObject) || checker2.check(jsonObject)
}