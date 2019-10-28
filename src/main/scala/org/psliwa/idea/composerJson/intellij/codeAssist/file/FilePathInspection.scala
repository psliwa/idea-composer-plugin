package org.psliwa.idea.composerJson.intellij.codeAssist.file

import com.intellij.codeInspection.{LocalQuickFix, ProblemsHolder}
import com.intellij.json.psi.JsonProperty
import com.intellij.psi.{PsiDirectory, PsiElement}
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.intellij.codeAssist.{AbstractInspection, RemoveJsonElementQuickFix}
import org.psliwa.idea.composerJson.intellij.PsiExtractors
import PsiExtractors.{JsonArray, JsonObject, JsonStringLiteral}
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.ProblemDescriptor
import org.psliwa.idea.composerJson.json._
import org.psliwa.idea.composerJson.util.Files._

class FilePathInspection extends AbstractInspection {
  // TODO: refactor with SchemaInspection or move common logic to AbstractInspection
  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    val collectedProblems = collectProblems(element, schema).toSet
    collectedProblems.foreach(problem => problems.registerProblem(problem.element, problem.message.getOrElse(""), problem.quickFixes:_*))
  }

  private def collectProblems(element: PsiElement, schema: Schema): Seq[ProblemDescriptor[LocalQuickFix]] = {
    import scala.collection.JavaConverters._

    val maybeRootDir = Option(element.getContainingFile).flatMap(file => Option(file.getContainingDirectory))

    maybeRootDir match {
      case Some(rootDir) =>
        schema match {
          case SObject(schemaProperties, _) => element match {
            case JsonObject(properties) => {
              for {
                property <- properties.asScala
                schemaProperty <- schemaProperties.get(property.getName).toList
                value <- Option(property.getValue).toList
                problem <- collectProblems(value, schemaProperty.schema)
              } yield problem
            }
            case _ =>
              List.empty
          }
          case SArray(itemType) => element match {
            case JsonArray(values) =>
              values.asScala.flatMap(collectProblems(_, itemType))
            case _ =>
              List.empty
          }
          case SOr(items) =>
            items.flatMap(collectProblems(element, _))
          case SFilePath(true) => element match {
            case jsl@JsonStringLiteral(value) => {
              if(!pathExists(rootDir, value)) {
                List(
                  ProblemDescriptor(element, ComposerBundle.message("inspection.filePath.fileMissing", value), Seq(
                    CreateFilesystemItemQuickFix.file(jsl), CreateFilesystemItemQuickFix.directory(jsl), removeValueQuickFix(element)
                  ))
                )
              } else {
                List.empty
              }
            }
            case _ =>
              List.empty
          }
          case SFilePaths(true) => element match {
            case JsonObject(properties) =>
              properties.asScala.flatMap(property => Option(property.getValue)).flatMap(collectProblems(_, schema))
            case jsl@JsonStringLiteral(value) => {
              if(!pathExists(rootDir, value)) {
                List(
                  ProblemDescriptor(element, ComposerBundle.message("inspection.filePath.fileMissing", value), Seq(
                    CreateFilesystemItemQuickFix.file(jsl),
                    CreateFilesystemItemQuickFix.directory(jsl),
                    removePropertyQuickFix(getPropertyIfPossible(element))
                  ))
                )
              } else {
                List.empty
              }
            }
            case JsonArray(values) =>
              values.asScala.flatMap(collectProblems(_, schema))
            case _ =>
              List.empty
          }
          case _ =>
            List.empty
        }
      case None =>
        List.empty
    }
  }

  private def getPropertyIfPossible(e: PsiElement): PsiElement = e.getContext match {
    case x: JsonProperty => x
    case _ => e
  }

  private def removePropertyQuickFix(element: PsiElement): RemoveJsonElementQuickFix = {
    new RemoveJsonElementQuickFix(element, ComposerBundle.message("inspection.quickfix.removeEntry"))
  }

  private def removeValueQuickFix(element: PsiElement): RemoveJsonElementQuickFix = {
    new RemoveJsonElementQuickFix(element, ComposerBundle.message("inspection.quickfix.removeEntry"))
  }

  private def pathExists(rootDir: PsiDirectory, path: String): Boolean = {
    findPath(rootDir, path).isDefined
  }
}
