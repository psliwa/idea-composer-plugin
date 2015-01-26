package org.psliwa.idea.composerJson.intellij.codeAssist.problem

import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.intellij.PsiExtractors

private[codeAssist] sealed trait Condition {
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
private[codeAssist] case class ConditionIs(value: Any) extends Condition
private[codeAssist] object ConditionExists extends Condition
private[codeAssist] case class ConditionIsNot(value: Any) extends Condition
private[codeAssist] case class ConditionNot(condition: Condition) extends Condition

private[codeAssist] object Condition {
  import PsiExtractors._
  def getValue(element: PsiElement) = {
    element match {
      case JsonStringLiteral(value) => Some(value)
      case JsonBooleanLiteral(value) => Some(value)
      case _ => None
    }
  }
}