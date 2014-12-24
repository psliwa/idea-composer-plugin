package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInspection.{ProblemsHolder, ProblemDescriptor, InspectionManager, LocalInspectionTool}
import com.intellij.json.psi._
import com.intellij.psi.{PsiElementVisitor, PsiElement, PsiFile}
import org.psliwa.idea.composerJson.json._
import org.psliwa.idea.composerJson.{ComposerBundle, ComposerJson}
import org.psliwa.idea.composerJson.composer._

class SchemaInspection extends LocalInspectionTool {

  val schema = SchemaInspection.schema
  val vowels = "aeiou"

  override def checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    if(file.getName != ComposerJson) Array()
    else doCheckFile(file, manager, isOnTheFly)
  }

  private def doCheckFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    val problems = new ProblemsHolder(manager, file, isOnTheFly)

    for {
      file <- ensureJsonFile(file)
      schema <- schema
      topLevelValue <- Option(file.getTopLevelValue)
    } yield collectProblems(topLevelValue, schema, problems)

    problems.getResultsArray
  }

  private def ensureJsonFile(file: PsiFile) = file match {
    case x: JsonFile => Some(x)
    case _ => None
  }

  private def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    import scala.collection.JavaConversions._

    schema match {
      case so@SObject(schemaProperties, additionalProperties) => element match {
        case JsonObject(properties) => {
          for(property <- properties) {
            schemaProperties.get(property.getName) match {
              case Some(schemaProperty) => Option(property.getValue).foreach(collectProblems(_, schemaProperty.schema, problems))
              case None if !additionalProperties => problems.registerProblem(
                property.getNameElement,
                ComposerBundle.message("inspection.schema.notAllowedProperty", property.getName)
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
      case SPackages | SFilePaths => element match {
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
      case SFilePath => element match {
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
          //TODO: add fix - remove quotes if string inside is "true" or "false"
          registerInvalidTypeProblem(problems, element, schema)
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
          //TODO: add fix - remove quotes when string inside quotes is numeric
          registerInvalidTypeProblem(problems, element, schema)
        }
      }
      case _ =>
    }
  }

  private def registerInvalidTypeProblem(problems: ProblemsHolder, element: PsiElement, schema: Schema) {
    problems.registerProblem(
      element,
      ComposerBundle.message("inspection.schema.type", prependPrefix(readableType(schema)), readableType(element))
    )
  }

  private def readableType(s: Schema): String = s match {
    case SObject(_, _) | SPackages | SFilePaths => "object"
    case SArray(_) => "array"
    case SBoolean => "boolean"
    case SString(_) | SFilePath | SStringChoice(_) => "string"
    case SNumber => "integer"
    case SOr(as) => as.map(readableType).mkString(" or ")
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

  private object JsonFile {
    def unapply(x: JsonFile): Option[(JsonValue)] = Option(x.getTopLevelValue)
  }

  private object JsonObject {
    def unapply(x: JsonObject): Option[(java.util.List[JsonProperty])] = Some(x.getPropertyList)
  }

  private object JsonProperty {
    def unapply(x: JsonProperty): Option[(String, JsonValue)] = Some((x.getName, x.getValue))
  }

  private object JsonArray {
    def unapply(x: JsonArray): Option[(java.util.List[JsonValue])] = Some(x.getValueList)
  }

  private object JsonValue {
    def unapply(x: JsonValue): Option[(String)] = Option(x.getText)
  }

  private object JsonStringLiteral {
    import scala.collection.JavaConversions._
    def unapply(x: JsonStringLiteral): Option[(String)] = x.getTextFragments.headOption.map(_.second).orElse(Some(""))
  }

  private object JsonBooleanLiteral {
    def unapply(x: JsonBooleanLiteral): Option[(Boolean)] = Some(x.getText.toBoolean)
  }

  private object JsonNumberLiteral {
    def unapply(x: JsonNumberLiteral): Option[(Unit)] = Some(())
  }
}

private object SchemaInspection {
  lazy val schema = SchemaLoader.load(ComposerSchemaFilepath)
}
