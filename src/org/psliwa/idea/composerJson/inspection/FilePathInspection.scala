package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.{PsiDirectory, PsiElement}
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.inspection.PsiExtractors.{JsonStringLiteral, JsonArray, JsonObject}
import org.psliwa.idea.composerJson.json._
import org.psliwa.idea.composerJson.util.Files._

class FilePathInspection extends Inspection {
  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    import scala.collection.JavaConversions._

    val rootDir = element.getContainingFile.getContainingDirectory

    schema match {
      case SObject(schemaProperties, _) => element match {
        case JsonObject(properties) => {
          properties.foreach(property => {
            schemaProperties.get(property.getName).foreach(schemaProperty => {
              Option(property.getValue).foreach(collectProblems(_, schemaProperty.schema, problems))
            })
          })
        }
        case _ =>
      }
      case SArray(itemType) => element match {
        case JsonArray(values) => for(value <- values) {
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
        case JsonObject(properties) => for(property <- properties) {
          Option(property.getValue).foreach(collectProblems(_, schema, problems))
        }
        case jsl@JsonStringLiteral(value) => {
          if(!pathExists(rootDir, value)) {
            problems.registerProblem(
              element,
              ComposerBundle.message("inspection.filePath.fileMissing", value),
              CreateFilesystemItemQuickFix.file(jsl) :: CreateFilesystemItemQuickFix.directory(jsl) :: Option(element.getContext).map(removePropertyQuickFix).toList:_*
            )
          }
        }
      }
      case _ =>
    }
  }

  def removePropertyQuickFix(element: PsiElement): RemoveJsonElementQuickFix = {
    new RemoveJsonElementQuickFix(element, ComposerBundle.message("inspection.quickfix.removeProperty"))
  }

  private def removeValueQuickFix(element: PsiElement): RemoveJsonElementQuickFix = {
    new RemoveJsonElementQuickFix(element, ComposerBundle.message("inspection.quickfix.removeValue"))
  }

  private def pathExists(rootDir: PsiDirectory, path: String): Boolean = {
    findPath(rootDir, path).isDefined
  }
}
