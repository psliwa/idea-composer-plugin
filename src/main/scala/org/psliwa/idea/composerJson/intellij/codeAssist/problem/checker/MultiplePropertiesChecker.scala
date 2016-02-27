package org.psliwa.idea.composerJson.intellij.codeAssist.problem.checker

import com.intellij.json.psi.JsonObject
import org.psliwa.idea.composerJson.intellij.PsiElements._
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.PropertyPath._
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.{Condition, PropertyPath}

import scala.collection.JavaConversions._

private[codeAssist] case class MultiplePropertiesChecker(propertyPath: PropertyPath, condition: Condition) extends Checker {
  override def check(jsonObject: JsonObject): CheckResult = {
    val propertyPaths = (for {
      property <- findPropertyInPath(jsonObject, propertyPath).toList
      propertyValue <-  Option(property.getValue).toList
      propertyObject <- ensureJsonObject(propertyValue).toList
      propertyName <- propertyObject.getPropertyList.map(_.getName)
    } yield propertyPath / propertyName).toSet

    propertyPaths.filter(condition.check(jsonObject, _)) match {
      case properties if properties.isEmpty => CheckResult(value = false, Set.empty)
      case properties => CheckResult(value = true, properties)
    }
  }
}
