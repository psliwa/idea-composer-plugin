package org.psliwa.idea.composerJson.inspection.problem

import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.inspection.PsiExtractors

private[inspection] sealed trait Condition {
  import Condition._

  def check(jsonObject: JsonObject, propertyName: String): Boolean = {
    val result = for {
      property <- Option(jsonObject.findProperty(propertyName))
      value <- getValue(property.getValue)
    } yield this match {
        case ConditionIs(expected) => value == expected
        case ConditionIsNot(expected) => value != expected
        case ConditionNot(condition) => !condition.check(jsonObject, propertyName)
        case ConditionExists => true
      }

    result.getOrElse(false)
  }
}
private case class ConditionIs(value: Any) extends Condition
private object ConditionExists extends Condition
private case class ConditionIsNot(value: Any) extends Condition
private case class ConditionNot(condition: Condition) extends Condition

private object Condition {
  import PsiExtractors._
  def getValue(element: PsiElement) = {
    element match {
      case JsonStringLiteral(value) => Some(value)
      case JsonBooleanLiteral(value) => Some(value)
      case _ => None
    }
  }
}