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

import scala.collection.immutable

class FilePathInspection extends AbstractInspection {
  // TODO: refactor with SchemaInspection or move common logic to AbstractInspection
  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    val collectedProblems = collectProblems(element, schema).toSet.flatten
    collectedProblems.foreach(problem => problems.registerProblem(problem.element, problem.message.getOrElse(""), problem.quickFixes:_*))
  }

  private def collectProblems(element: PsiElement, schema: Schema): Option[Seq[ProblemDescriptor[LocalQuickFix]]] = {
    import scala.collection.JavaConverters._

    val maybeRootDir = Option(element.getContainingFile).flatMap(file => Option(file.getContainingDirectory))

    maybeRootDir match {
      case Some(rootDir) =>
        schema match {
          case SObject(schemaProperties, _) => element match {
            case JsonObject(properties) => {
              val problemsPerProperty = for {
                property <- properties.asScala
                schemaProperty <- schemaProperties.get(property.getName).toList
                value <- Option(property.getValue).toList
                problems = collectProblems(value, schemaProperty.schema)
              } yield problems

              bestMatchingProblems(problemsPerProperty)
            }
            case _ =>
              None
          }
          case SArray(itemType) => element match {
            case JsonArray(values) =>
              val problems = values.asScala.toList.map(collectProblems(_, itemType))
              if(problems.forall(_.isEmpty)) {
                None
              } else {
                Some(problems.flatten.flatten)
              }
            case _ =>
              None
          }
          case SOr(items) =>
            val problemsPerItem = items.map(collectProblems(element, _))
            problemsPerItem.collect {
              case Some(problems) => problems
            }.sortBy(_.size).headOption
          case SFilePath(true) => element match {
            case jsl@JsonStringLiteral(value) => {
              if(!pathExists(rootDir, value)) {
                Some(List(
                  ProblemDescriptor(element, ComposerBundle.message("inspection.filePath.fileMissing", value), Seq(
                    CreateFilesystemItemQuickFix.file(jsl), CreateFilesystemItemQuickFix.directory(jsl), removeValueQuickFix(element)
                  ))
                ))
              } else {
                Some(List.empty)
              }
            }
            case _ =>
              None
          }
          case SFilePaths(true) => element match {
            case JsonObject(properties) =>
              val problems = properties.asScala.flatMap(property => Option(property.getValue)).map(collectProblems(_, schema))
              bestMatchingProblems(problems)
            case jsl@JsonStringLiteral(value) => {
              if(!pathExists(rootDir, value)) {
                Some(List(
                  ProblemDescriptor(element, ComposerBundle.message("inspection.filePath.fileMissing", value), Seq(
                    CreateFilesystemItemQuickFix.file(jsl),
                    CreateFilesystemItemQuickFix.directory(jsl),
                    removePropertyQuickFix(getPropertyIfPossible(element))
                  ))
                ))
              } else {
                Some(List.empty)
              }
            }
            case JsonArray(values) =>
              val problems: immutable.Seq[Option[Seq[ProblemDescriptor[LocalQuickFix]]]] = values.asScala.toList.map(collectProblems(_, schema))
              bestMatchingProblems(problems)
            case _ =>
              None
          }
          case _: SString => element match {
            case JsonStringLiteral(_) =>
              Some(List.empty)
            case _ =>
              None
          }
          case _ =>
            None
        }
      case None =>
        None
    }
  }

  private def bestMatchingProblems[Problem](problems: Seq[Option[Seq[Problem]]]): Option[Seq[Problem]] = {
    if(problems.forall(_.isEmpty)) {
      None
    } else {
      Some(problems.flatten.flatten)
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
