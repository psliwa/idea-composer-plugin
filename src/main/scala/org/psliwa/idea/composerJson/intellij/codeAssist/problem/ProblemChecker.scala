package org.psliwa.idea.composerJson.intellij.codeAssist.problem

import com.intellij.codeInspection.{LocalQuickFixOnPsiElement, ProblemHighlightType}
import com.intellij.json.psi.{JsonProperty, JsonObject}

import scala.collection.JavaConversions._
import scala.language.implicitConversions
import scala.util.matching.Regex

private[codeAssist] case class ProblemChecker(
  checker: Checker,
  problem: String,
  createQuickFixes: (JsonObject, String) => List[LocalQuickFixOnPsiElement],
  highlightType: ProblemHighlightType = ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
  elements: JsonObject => List[JsonObject] = List(_: JsonObject)
) extends Checker {
  override def check(jsonObject: JsonObject): CheckResult = checker.check(jsonObject)
}

private[codeAssist] trait Checker {
  def check(jsonObject: JsonObject): CheckResult

  def &&(checker: Checker): Checker = AndChecker(this, checker)
  def ||(checker: Checker): Checker = OrChecker(this, checker)
}

private[codeAssist] case class CheckResult(value: Boolean, properties: Set[(JsonObject,String)]) {
  lazy val not = CheckResult(!value, properties)

  def &&(result: CheckResult) = CheckResult(value && result.value, properties ++ result.properties)

  def ||(result: CheckResult) = CheckResult(value || result.value, properties ++ result.properties)
}

private[codeAssist] case class PropertyChecker(propertyName: String, condition: Condition = ConditionExists) extends Checker {
  import org.psliwa.idea.composerJson.intellij.PsiElements._

  def is(value: Any) = copy(condition = ConditionIs(value))
  def isNot(value: Any) = copy(condition = ConditionIsNot(value))
  def matches(regex: Regex) = copy(condition = ConditionMatch(regex))
  def duplicatesSibling(siblingPropertyName: String) = {
    def sibling(jsonObject: JsonObject): Option[JsonObject] = for {
      parent <- Option(jsonObject.getParent).flatMap(e => Option(e.getParent))
      parentObject <- ensureJsonObject(parent)
      sibling <- Option(parentObject.findProperty(siblingPropertyName))
      siblingObject <- ensureJsonObject(sibling.getValue)
    } yield siblingObject

    def properties(jsonObject: JsonObject): Set[(JsonObject, String)] = for {
      property: JsonProperty <- Option(jsonObject.findProperty(propertyName)).toSet
      propertyValue: JsonObject <- ensureJsonObject(property.getValue).toSet
      name: String <- propertyValue.getPropertyList.map(_.getName).toSet
    } yield (propertyValue, name)

    MultiplePropertiesChecker(properties, ConditionDuplicateIn(sibling))
  }

  override def check(jsonObject: JsonObject): CheckResult = CheckResult(condition.check(jsonObject, propertyName), Set(jsonObject -> propertyName))
}

private[codeAssist] case class MultiplePropertiesChecker(getProperties: JsonObject => Set[(JsonObject, String)], condition: Condition) extends Checker {
  override def check(jsonObject: JsonObject): CheckResult = {
    getProperties(jsonObject).filter { case(propertyParent, property) => condition.check(propertyParent, property) } match {
      case properties if properties.isEmpty => CheckResult(value = false, Set.empty)
      case properties => CheckResult(value = true, properties)
    }
  }
}

private[codeAssist] object Checker {
  def not(checker: Checker) = new Checker {
    override def check(jsonObject: JsonObject): CheckResult = checker.check(jsonObject).not
  }
}

private[codeAssist] object ImplicitConversions {
  implicit def stringToProblemChecker(property: String): PropertyChecker = PropertyChecker(property)
}

private[codeAssist] case class AndChecker(checker1: Checker, checker2: Checker) extends Checker{
  override def check(jsonObject: JsonObject): CheckResult = checker1.check(jsonObject) && checker2.check(jsonObject)
}

private[codeAssist] case class OrChecker(checker1: Checker, checker2: Checker) extends Checker{
  override def check(jsonObject: JsonObject): CheckResult = checker1.check(jsonObject) || checker2.check(jsonObject)
}