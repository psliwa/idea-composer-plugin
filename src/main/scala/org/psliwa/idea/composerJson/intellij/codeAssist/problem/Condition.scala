package org.psliwa.idea.composerJson.intellij.codeAssist.problem

import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.intellij.PsiExtractors
import PropertyPath._
import org.psliwa.idea.composerJson.intellij.PsiElements._

import scala.util.matching.Regex

private[codeAssist] sealed trait Condition {
  import Condition._

  def check(jsonObject: JsonObject, propertyPath: PropertyPath): Boolean = {
    val result = for {
      property <- findPropertyInPath(jsonObject, propertyPath)
      value <- getValue(property.getValue)
    } yield this match {
        case ConditionIs(expected) => value == expected
        case ConditionIsNot(expected) => value != expected
        case ConditionNot(condition) => !condition.check(jsonObject, propertyPath)
        case ConditionMatch(pattern) => pattern.findFirstIn(value.toString).isDefined
        case ConditionDuplicateIn(dependencyPropertyPath) => (for {
          dependencyProperty <- findPropertyInPath(jsonObject, dependencyPropertyPath)
          dependencyObject <- Option(dependencyProperty.getValue).flatMap(ensureJsonObject)
          duplicatedProperty <- findPropertyInPath(dependencyObject, PropertyPath(propertyPath.lastProperty, List.empty))
        } yield true).getOrElse(false)
        case ConditionExists => true
      }

    result.getOrElse(false)
  }
}
private[codeAssist] case class ConditionIs(value: Any) extends Condition
private[codeAssist] case class ConditionMatch(regex: Regex) extends Condition
private[codeAssist] object ConditionExists extends Condition
private[codeAssist] case class ConditionIsNot(value: Any) extends Condition
private[codeAssist] case class ConditionNot(condition: Condition) extends Condition
private[codeAssist] case class ConditionDuplicateIn(dependencyPropertyPath: PropertyPath) extends Condition

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