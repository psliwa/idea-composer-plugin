package org.psliwa.idea.composerJson.intellij.codeAssist.problem

import com.intellij.json.psi.{JsonObject, JsonProperty}
import org.psliwa.idea.composerJson.intellij.PsiElements._

import scala.annotation.tailrec

private[codeAssist] case class PropertyPath(headProperty: String, tailProperties: List[String]) {
  def /(property: String) = copy(tailProperties = tailProperties ++ List(property))

  lazy val lastProperty: String = (headProperty :: tailProperties).last
}

private[codeAssist] object PropertyPath {
  def findPropertyInPath(jsonObject: JsonObject, propertyPath: PropertyPath): Option[JsonProperty] = {
    @tailrec
    def loop(jsonObject: Option[JsonObject], propertyPath: PropertyPath, foundProperty: Option[JsonProperty]): Option[JsonProperty] = {
      (jsonObject.flatMap(o => Option(o.findProperty(propertyPath.headProperty))), propertyPath) match {
        case (Some(property), PropertyPath(_, Nil)) => Some(property)
        case (Some(property), PropertyPath(_, head :: tail)) => loop(ensureJsonObject(property.getValue), PropertyPath(head, tail), Some(property))
        case _ => None
      }
    }

    loop(Some(jsonObject), propertyPath, None)
  }

  def siblingPropertyPath(propertyPath: PropertyPath, siblingPropertyName: String): PropertyPath = propertyPath match {
    case PropertyPath(_, Nil) => PropertyPath(siblingPropertyName, List.empty)
    case PropertyPath(rootProperty, tailProperties) => PropertyPath(rootProperty, tailProperties.dropRight(1) ++ List(siblingPropertyName))
  }
}
