package org.psliwa.idea.composerJson.intellij.codeAssist.schema

import com.intellij.codeInspection.{LocalQuickFix, ProblemsHolder}
import com.intellij.json.psi.{JsonNumberLiteral, JsonProperty, _}
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.ProblemDescriptor
import org.psliwa.idea.composerJson.intellij.codeAssist.{
  AbstractInspection,
  CreatePropertyQuickFix,
  RemoveJsonElementQuickFix
}
import org.psliwa.idea.composerJson.intellij.PsiExtractors
import org.psliwa.idea.composerJson.json._

import scala.util.matching.Regex

class SchemaInspection extends AbstractInspection {

  val vowels = "aeiou"
  val numberPattern = "^\"-?\\d+(\\.\\d+)?\"$".r
  val booleanPattern = "^\"(true|false)\"$".r

  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    val collectedProblems = collectProblems(element, schema).toSet
    collectedProblems.foreach(
      problem => problems.registerProblem(problem.element, problem.message.getOrElse(""), problem.quickFixes: _*)
    )
  }

  private def collectProblems(element: PsiElement, schema: Schema): Seq[ProblemDescriptor[LocalQuickFix]] = {
    import scala.jdk.CollectionConverters._
    import PsiExtractors._

    def collectNotAllowedPropertyProblems(property: JsonProperty,
                                          schemaProperties: Properties,
                                          additionalProperties: Boolean): Seq[ProblemDescriptor[LocalQuickFix]] = {
      schemaProperties.get(property.getName) match {
        case Some(schemaProperty) => Option(property.getValue).toList.flatMap(collectProblems(_, schemaProperty.schema))
        case None if !additionalProperties =>
          List(
            ProblemDescriptor(
              property.getNameElement.getContext,
              ComposerBundle.message("inspection.schema.notAllowedProperty", property.getName),
              Seq(removeJsonPropertyQuickFix(property))
            )
          )
        case _ => List.empty
      }
    }

    schema match {
      case so @ SObject(schemaProperties, additionalProperties) =>
        element match {
          case JsonObject(properties) => {
            val notAllowedPropertyProblems = for {
              property <- properties.asScala
              problem <- collectNotAllowedPropertyProblems(property, schemaProperties, additionalProperties)
            } yield problem

            val alreadyDefinedPropertiesProblems = getAlreadyDefinedProperties(properties.asScala.toList)
              .map(
                property =>
                  ProblemDescriptor(
                    property,
                    ComposerBundle.message("inspection.schema.alreadyDefinedProperty", property.getName),
                    Seq(removeJsonPropertyQuickFix(property))
                  )
              )
              .toList

            lazy val propertyNames = properties.asScala.map(_.getName).toSet

            val requiredPropertiesProblems =
              for ((name, property) <- so.requiredProperties if !propertyNames.contains(name)) yield {
                ProblemDescriptor(
                  element,
                  ComposerBundle.message("inspection.schema.required", name),
                  Seq(new CreatePropertyQuickFix(element, name, property.schema): LocalQuickFix)
                )
              }

            notAllowedPropertyProblems.toList ::: alreadyDefinedPropertiesProblems ::: requiredPropertiesProblems.toList
          }
          case _ => List(invalidTypeProblem(element, schema))
        }
      case SPackages | SFilePaths(_) =>
        element match {
          case JsonObject(_) => List.empty
          case _ => List(invalidTypeProblem(element, schema))
        }
      case SString(format) =>
        element match {
          case JsonStringLiteral(value) =>
            if (!format.isValid(value)) {
              List(
                ProblemDescriptor(
                  element,
                  ComposerBundle.message("inspection.schema.format", prependPrefix(readableFormat(format))),
                  Seq()
                )
              )
            } else {
              List.empty
            }
          case _ => List(invalidTypeProblem(element, schema))
        }
      case SFilePath(_) =>
        element match {
          case JsonStringLiteral(_) => List.empty
          case _ => List(invalidTypeProblem(element, schema))
        }
      case SStringChoice(choices) =>
        element match {
          case x @ JsonStringLiteral(value) if !choices.contains(value) => {
            List(
              ProblemDescriptor(
                element,
                ComposerBundle.message("inspection.schema.notAllowedPropertyValue",
                                       value,
                                       choices.map("'" + _ + "'").mkString(" or ")),
                Seq(new ShowValidValuesQuickFix(x))
              )
            )
          }
          case JsonStringLiteral(_) => List.empty
          case _ => List(invalidTypeProblem(element, schema))
        }
      case SBoolean =>
        element match {
          case JsonBooleanLiteral(_) => List.empty
          case _ =>
            List(invalidTypeProblem(element, schema, removeQuotesQuickFixWhenMatches(element, booleanPattern): _*))
        }
      case SArray(item) =>
        element match {
          case JsonArray(values) =>
            for {
              value <- values.asScala.toList
              problem <- collectProblems(value, item)
            } yield problem
          case _ => List(invalidTypeProblem(element, schema))
        }
      case SOr(items) => {
        // TODO: find the better matching item and report problems for it
        val problemsForItems = items.map(collectProblems(element, _))
        problemsForItems.find(_.isEmpty) match {
          case Some(_) =>
            List.empty
          case None =>
            val problems = problemsForItems.filter(_.nonEmpty).flatten
            val innerProblems = problems.filterNot(_.element == element)

            if (innerProblems.nonEmpty) innerProblems
            else if (problems.size >= items.length) List(invalidTypeProblem(element, schema))
            else List.empty
        }
      }
      case SNumber =>
        element match {
          case PsiExtractors.JsonNumberLiteral(_) => List.empty
          case _ =>
            List(invalidTypeProblem(element, schema, removeQuotesQuickFixWhenMatches(element, numberPattern): _*))
        }
      case _ => List.empty
    }
  }

  override def getShortName: String = "ComposerJsonSchema"

  private def getAlreadyDefinedProperties(properties: List[JsonProperty]): Iterable[JsonProperty] = {
    properties.view
      .groupBy(_.getName)
      .filter(_._2.size > 1)
      .map { case (key, values) => key -> values.tail }
      .values
      .flatten
  }

  private def removeJsonPropertyQuickFix(property: JsonProperty): LocalQuickFix = {
    new RemoveJsonElementQuickFix(property, ComposerBundle.message("inspection.quickfix.removeEntry"))
  }

  private def removeQuotesQuickFixWhenMatches(e: PsiElement, pattern: Regex): List[LocalQuickFix] = {
    if (pattern.findFirstMatchIn(e.getText).isDefined) {
      List(new RemoveQuotesQuickFix(e))
    } else {
      Nil
    }
  }

  private def invalidTypeProblem(element: PsiElement, schema: Schema, quickFixes: LocalQuickFix*) = {
    ProblemDescriptor(
      element,
      Some(
        ComposerBundle.message("inspection.schema.type", prependPrefix(readableType(schema)), readableType(element))
      ),
      quickFixes
    )
  }

  private def readableType(s: Schema): String = s match {
    case SObject(_, _) | SPackages | SFilePaths(_) => "object"
    case SArray(_) => "array"
    case SBoolean => "boolean"
    case SString(_) | SFilePath(_) | SStringChoice(_) => "string"
    case SNumber => "integer"
    case SOr(as) => as.map(readableType).distinct.mkString(" or ")
    case _ => "unknown"
  }

  private def readableType(e: PsiElement): String = e match {
    case _: JsonObject => "object"
    case _: JsonArray => "array"
    case _: JsonStringLiteral => "string"
    case _: JsonBooleanLiteral => "boolean"
    case _: JsonNumberLiteral => "number"
    case _ => "unknown"
  }

  private def readableFormat(format: Format): String = format match {
    case EmailFormat => "email"
    case UriFormat => "uri"
    case PatternFormat(pattern) => pattern.toString
    case _ => "unknown"
  }

  private def prependPrefix(s: String) = prefix(s) + " " + s
  private def prefix(s: String) = if (vowels.contains(s(0))) "an" else "a"
}
