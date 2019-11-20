package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.codeInspection.{ProblemHighlightType, ProblemsHolder}
import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.intellij.PsiElements
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.checker.{Checker, ImplicitConversions}
import org.psliwa.idea.composerJson.intellij.codeAssist.{
  AbstractInspection,
  CreatePropertyQuickFix,
  RemoveJsonElementQuickFix,
  SetPropertyValueQuickFix
}
import Checker._
import ImplicitConversions._
import org.psliwa.idea.composerJson.intellij.codeAssist.problem._
import org.psliwa.idea.composerJson.json.{SBoolean, SString, SStringChoice, Schema}
import PsiElements._
import PropertyPath._
import org.psliwa.idea.composerJson.composer.model.PackageDescriptor

class MisconfigurationInspection extends AbstractInspection {

  private val problemCheckers = List(
    ProblemChecker(
      ("type" is "project") && ("minimum-stability" isNot "stable") &&
        (("prefer-stable" is false) || not("prefer-stable")),
      ComposerBundle.message("inspection.misconfig.notStableProject"),
      (jsonObject, _) =>
        List(
          new SetPropertyValueQuickFix(jsonObject, "prefer-stable", SBoolean, "true"),
          new SetPropertyValueQuickFix(jsonObject, "minimum-stability", SString(), "stable")
        )
    ),
    ProblemChecker(
      not("license") && "name",
      ComposerBundle.message("inspection.misconfig.missingLicense"),
      (jsonObject, _) =>
        List(
          new CreatePropertyQuickFix(jsonObject, "license", SString())
        ),
      ProblemHighlightType.WEAK_WARNING
    ),
    ProblemChecker(
      "type" is "composer-installer",
      ComposerBundle.message("inspection.misconfig.composerInstaller"),
      (jsonObject, _) =>
        List(
          new SetPropertyValueQuickFix(jsonObject, "type", SStringChoice(List.empty), "composer-plugin")
        ),
      ProblemHighlightType.WEAK_WARNING
    ),
    ProblemChecker(
      "name" && ("name" matches "[A-Z]".r),
      ComposerBundle.message("inspection.misconfig.camelCaseName"),
      (jsonObject, _) => {
        (for {
          nameProperty <- Some(jsonObject.findProperty("name"))
          nameValue <- Some(nameProperty.getValue)
          name <- getStringValue(nameValue)
        } yield new SetPropertyValueQuickFix(jsonObject, "name", SString(), PackageDescriptor.fixName(name))).toList
      },
      ProblemHighlightType.WEAK_WARNING
    ),
    ProblemChecker(
      ("require" duplicatesSibling "require-dev") || ("require-dev" duplicatesSibling "require"),
      ComposerBundle.message("inspection.misconfig.requireDuplication"),
      (jsonObject, propertyPath) => {
        findPropertiesInPath(jsonObject, propertyPath)
          .map(
            new RemoveJsonElementQuickFix(_,
                                          ComposerBundle.message("inspection.quickfix.removeDependency",
                                                                 propertyPath.lastProperty))
          )
      }
    ),
    ProblemChecker(
      ("autoload" / "psr-0" / "") || ("autoload" / "psr-4" / ""),
      ComposerBundle.message("inspection.misconfig.emptyPsrNamespace"),
      (jsonObject, _) => List.empty
    ),
    ProblemChecker(
      "require" / "*" matches "#".r,
      ComposerBundle.message("inspection.misconfig.commitRefAsVersion")
    ),
    requiredPropertyForLibrary("name"),
    requiredPropertyForLibrary("description")
  )

  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    ensureJsonObject(element)
      .foreach(collectProblems(_, problems))
  }

  private def collectProblems(jsonObject: JsonObject, problems: ProblemsHolder): Unit = {
    val problemDescriptions = problemCheckers.flatMap(checker => {
      checker
        .elements(jsonObject)
        .map(element => (element, checker.check(element)))
        .filter(_._2.value)
        .flatMap {
          case (_, checkResult) =>
            for {
              propertyPath <- checkResult.properties
              property <- findPropertiesInPath(jsonObject, propertyPath)
              value <- Option(property.getValue)
            } yield ProblemDescriptor(
              element = value,
              message = Some(checker.problem),
              quickFixes = checker.createQuickFixes(jsonObject, propertyPath),
              highlightType = checker.highlightType
            )
        }
    })

    problemDescriptions.foreach(
      problem =>
        problems.registerProblem(problem.element.getContext,
                                 problem.message.getOrElse(""),
                                 problem.highlightType,
                                 problem.quickFixes: _*)
    )
  }

  private def requiredPropertyForLibrary(property: String) = ProblemChecker(
    ("type" isNot "project") && not(property),
    ComposerBundle.message("inspection.misconfig.requiredForLibrary", property),
    (jsonObject, _) =>
      List(
        new CreatePropertyQuickFix(jsonObject, property, new SString()),
        new SetPropertyValueQuickFix(jsonObject, "type", new SString(), "project")
      ),
    ProblemHighlightType.GENERIC_ERROR
  )
}
