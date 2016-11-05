package org.psliwa.idea.composerJson.intellij.codeAssist.file

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonProperty
import com.intellij.psi.{PsiDirectory, PsiElement}
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.intellij.codeAssist.{AbstractInspection, RemoveJsonElementQuickFix}
import org.psliwa.idea.composerJson.intellij.PsiExtractors
import PsiExtractors.{JsonArray, JsonObject, JsonStringLiteral}
import org.psliwa.idea.composerJson.json._
import org.psliwa.idea.composerJson.util.Files._

class FilePathInspection extends AbstractInspection {
  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    import scala.collection.JavaConverters._

    val maybeRootDir = Option(element.getContainingFile).flatMap(file => Option(file.getContainingDirectory))

    maybeRootDir match {
      case Some(rootDir) =>
        schema match {
          case SObject(schemaProperties, _) => element match {
            case JsonObject(properties) => {
              properties.asScala.foreach(property => {
                schemaProperties.get(property.getName).foreach(schemaProperty => {
                  Option(property.getValue).foreach(collectProblems(_, schemaProperty.schema, problems))
                })
              })
            }
            case _ =>
          }
          case SArray(itemType) => element match {
            case JsonArray(values) => for(value <- values.asScala) {
              collectProblems(value, itemType, problems)
            }
            case _ =>
          }
          case SFilePath(true) => element match {
            case jsl@JsonStringLiteral(value) => {
              if(!pathExists(rootDir, value)) {
                problems.registerProblem(
                  element,
                  ComposerBundle.message("inspection.filePath.fileMissing", value),
                  CreateFilesystemItemQuickFix.file(jsl), CreateFilesystemItemQuickFix.directory(jsl), removeValueQuickFix(element)
                )
              }
            }
            case _ =>
          }
          case SFilePaths(true) => element match {
            case JsonObject(properties) => for(property <- properties.asScala) {
              Option(property.getValue).foreach(collectProblems(_, schema, problems))
            }
            case jsl@JsonStringLiteral(value) => {
              if(!pathExists(rootDir, value)) {
                problems.registerProblem(
                  element,
                  ComposerBundle.message("inspection.filePath.fileMissing", value),
                  CreateFilesystemItemQuickFix.file(jsl),
                  CreateFilesystemItemQuickFix.directory(jsl),
                  removePropertyQuickFix(getPropertyIfPossible(element))
                )
              }
            }
            case JsonArray(values) => for(value <- values.asScala) {
              collectProblems(value, schema, problems)
            }
            case _ =>
          }
          case _ =>
        }
      case None =>
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
