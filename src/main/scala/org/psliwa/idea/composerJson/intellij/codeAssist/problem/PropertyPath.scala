package org.psliwa.idea.composerJson.intellij.codeAssist.problem

import com.intellij.json.psi.{JsonObject, JsonProperty}
import org.psliwa.idea.composerJson.intellij.PsiElements._
import scala.jdk.CollectionConverters._

import scala.annotation.tailrec

/***
  * "*" char can be used as wildcard
  */
private[codeAssist] case class PropertyPath(headProperty: String, tailProperties: List[String]) {
  def /(property: String): PropertyPath = copy(tailProperties = tailProperties ++ List(property))

  lazy val lastProperty: String = (headProperty :: tailProperties).last
}

private[codeAssist] object PropertyPath {
  def findPropertiesInPath(jsonObject: JsonObject, propertyPath: PropertyPath): List[JsonProperty] = {
    @tailrec
    def loop(jsonObjects: List[JsonObject],
             propertyPath: PropertyPath,
             foundProperties: List[JsonProperty]): List[JsonProperty] = {
      (jsonObjects.flatMap(findProperties(_, propertyPath.headProperty)), propertyPath) match {
        case (properties, PropertyPath(_, Nil)) => properties
        case (properties, PropertyPath(_, head :: tail)) =>
          val newJsonObjects: List[JsonObject] =
            properties.flatMap(property => Option(property.getValue)).flatMap(ensureJsonObject)
          loop(newJsonObjects, PropertyPath(head, tail), properties)
        case _ => foundProperties
      }
    }

    def findProperties(jsonObject: JsonObject, propertyName: String): List[JsonProperty] = propertyName match {
      case "*" => jsonObject.getPropertyList.asScala.toList
      case name => Option(jsonObject.findProperty(name)).toList
    }

    loop(List(jsonObject), propertyPath, List.empty)
  }

  def siblingPropertyPath(propertyPath: PropertyPath, siblingPropertyName: String): PropertyPath = propertyPath match {
    case PropertyPath(_, Nil) => PropertyPath(siblingPropertyName, List.empty)
    case PropertyPath(rootProperty, tailProperties) =>
      PropertyPath(rootProperty, tailProperties.dropRight(1) ++ List(siblingPropertyName))
  }
}
