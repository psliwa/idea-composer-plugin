package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInspection._
import com.intellij.json.psi._
import com.intellij.psi.{PsiElement, PsiFile}
import org.psliwa.idea.composerJson.json._
import org.psliwa.idea.composerJson.{ComposerBundle, ComposerJson}
import org.psliwa.idea.composerJson.ComposerSchema

import scala.util.matching.Regex

class SchemaInspection extends Inspection {

  val vowels = "aeiou"
  val numberPattern = "^\"-?\\d+(\\.\\d+)?\"$".r
  val booleanPattern = "^\"(true|false)\"$".r

  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    import scala.collection.JavaConversions._
    import PsiExtractors._

    schema match {
      case so@SObject(schemaProperties, additionalProperties) => element match {
        case JsonObject(properties) => {
          for(property <- properties) {
            schemaProperties.get(property.getName) match {
              case Some(schemaProperty) => Option(property.getValue).foreach(collectProblems(_, schemaProperty.schema, problems))
              case None if !additionalProperties => problems.registerProblem(
                property.getNameElement.getContext,
                ComposerBundle.message("inspection.schema.notAllowedProperty", property.getName),
                removeJsonPropertyQuickFix(property)
              )
              case _ =>
            }
          }

          lazy val propertyNames = properties.map(_.getName).toSet

          for((name, property) <- so.requiredProperties if !propertyNames.contains(name)) {
            problems.registerProblem(
              element,
              ComposerBundle.message("inspection.schema.required", name)
            )
          }
        }
        case _ => registerInvalidTypeProblem(problems, element, schema)
      }
      case SPackages | SFilePaths(_) => element match {
        case JsonObject(_) =>
        case _ => registerInvalidTypeProblem(problems, element, schema)
      }
      case SString(format) => element match {
        case JsonStringLiteral(value) => if(!format.isValid(value)) {
          problems.registerProblem(
            element,
            ComposerBundle.message("inspection.schema.format", prependPrefix(readableFormat(format)))
          )
        }
        case _ => registerInvalidTypeProblem(problems, element, schema)
      }
      case SFilePath(_) => element match {
        case JsonStringLiteral(_) =>
        case _ => registerInvalidTypeProblem(problems, element, schema)
      }
      case SStringChoice(choices) => element match {
        case JsonStringLiteral(value) if !choices.contains(value) => {
          problems.registerProblem(
            element,
            ComposerBundle.message("inspection.schema.notAllowedPropertyValue", value, choices.map("'"+_+"'").mkString(" or "))
          )
        }
        case JsonStringLiteral(_) =>
        case _ => registerInvalidTypeProblem(problems, element, schema)
      }
      case SBoolean => element match {
        case JsonBooleanLiteral(_) =>
        case _ => {
          registerInvalidTypeProblem(problems, element, schema, removeQuotesQuickFixWhenMatches(element, booleanPattern):_*)
        }
      }
      case SArray(item) => element match {
        case JsonArray(values) => for(value <- values) {
          collectProblems(value, item, problems)
        }
        case _ => registerInvalidTypeProblem(problems, element, schema)
      }
      case SOr(items) => {
        val localProblems = new ProblemsHolder(problems.getManager, problems.getFile, problems.isOnTheFly)
        items.foreach {collectProblems(element, _, localProblems)}

        if(localProblems.getResultCount >= items.length) {
          registerInvalidTypeProblem(problems, element, schema)
        }
      }
      case SNumber => element match {
        case JsonNumberLiteral(_) =>
        case _ => {
          registerInvalidTypeProblem(problems, element, schema, removeQuotesQuickFixWhenMatches(element, numberPattern):_*)
        }
      }
      case _ =>
    }
  }

  private def removeJsonPropertyQuickFix(property: JsonProperty): RemoveJsonElementQuickFix = {
    new RemoveJsonElementQuickFix(property, ComposerBundle.message("inspection.quickfix.removeEntry"))
  }

  private def removeQuotesQuickFixWhenMatches(e: PsiElement, pattern: Regex): List[LocalQuickFix] = {
    if(pattern.findFirstMatchIn(e.getText).isDefined) {
      List(new RemoveQuotesQuickFix(e))
    } else {
      Nil
    }
  }

  private def registerInvalidTypeProblem(problems: ProblemsHolder, element: PsiElement, schema: Schema, quickFixes: LocalQuickFix*) {
    problems.registerProblem(
      element,
      ComposerBundle.message("inspection.schema.type", prependPrefix(readableType(schema)), readableType(element)),
      quickFixes: _*
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

  def readableFormat(format: Format): String = format match {
    case EmailFormat => "email"
    case UriFormat => "uri"
    case _ => "unknown"
  }

  private def prependPrefix(s: String) = prefix(s)+" "+s
  private def prefix(s: String) = if(vowels.contains(s(0))) "an" else "a"
}