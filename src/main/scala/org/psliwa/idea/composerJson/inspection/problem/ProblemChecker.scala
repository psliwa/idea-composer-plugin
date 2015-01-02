package org.psliwa.idea.composerJson.inspection.problem

import com.intellij.json.psi.JsonObject

import scala.language.implicitConversions

private[inspection] case class ProblemChecker(checker: Checker, properties: List[String], problem: String) extends Checker {
  override def check(jsonObject: JsonObject): Boolean = checker.check(jsonObject)
}

private[inspection] trait Checker {
  def check(jsonObject: JsonObject): Boolean

  def &&(checker: Checker): Checker = AndChecker(this, checker)
  def ||(checker: Checker): Checker = OrChecker(this, checker)
}

private[inspection] case class PropertyChecker(property: String, condition: Condition = ConditionExists) extends Checker {
  def is(value: Any) = copy(condition = ConditionIs(value))
  def isNot(value: Any) = copy(condition = ConditionIsNot(value))

  override def check(jsonObject: JsonObject): Boolean = condition.check(jsonObject, property)
}

private[inspection] object Checker {
  def not(checker: Checker) = new Checker {
    override def check(jsonObject: JsonObject): Boolean = !checker.check(jsonObject)
  }
}

private[inspection] object ImplicitConversions {
  implicit def stringToProblemChecker(property: String): PropertyChecker = PropertyChecker(property)
}

private case class AndChecker(checker1: Checker, checker2: Checker) extends Checker{
  override def check(jsonObject: JsonObject): Boolean = checker1.check(jsonObject) && checker2.check(jsonObject)
}

private case class OrChecker(checker1: Checker, checker2: Checker) extends Checker{
  override def check(jsonObject: JsonObject): Boolean = checker1.check(jsonObject) || checker2.check(jsonObject)
}