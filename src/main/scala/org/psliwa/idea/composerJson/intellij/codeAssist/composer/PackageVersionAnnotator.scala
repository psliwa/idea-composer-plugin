package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.json.psi._
import com.intellij.lang.annotation.{AnnotationHolder, Annotator, HighlightSeverity}
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.StandardPatterns._
import com.intellij.patterns.{PatternCondition, StringPattern}
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.composer.InstalledPackages
import org.psliwa.idea.composerJson.composer.version._
import org.psliwa.idea.composerJson.intellij.Patterns._
import org.psliwa.idea.composerJson.intellij.PsiElements._
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.ProblemDescriptor
import org.psliwa.idea.composerJson.intellij.codeAssist.{QuickFixIntentionActionAdapter, QuickFixIntentionActionAdapterWithPriority, SetPropertyValueQuickFix}
import org.psliwa.idea.composerJson.json.SString
import org.psliwa.idea.composerJson.settings.ProjectSettings

class PackageVersionAnnotator extends Annotator {
  import org.psliwa.idea.composerJson.intellij.codeAssist.composer.PackageVersionAnnotator._

  val suggestionHighlightSeverity = if(ApplicationManager.getApplication.isUnitTestMode) HighlightSeverity.INFORMATION
                                    else `HighlightSeverity.SUGGESTION`

  private type QuickFixGroup = (Option[String], Seq[IntentionAction])

  override def annotate(element: PsiElement, annotations: AnnotationHolder): Unit = {
    val pattern = psiElement().and(PackageVersionAnnotator.pattern)
      .withParent(psiElement().withName(and(stringContains("/"), not(excluded(element.getProject)))))

    if(pattern.accepts(element)) {
      val problemDescriptors = for {
        version <- getStringValue(element).toList
        pkg <- ensureJsonProperty(element.getParent).map(_.getName).toList
        (message, quickFixes) <- detectProblemsInVersion(pkg, version, element)
      } yield ProblemDescriptor(element, message, quickFixes)

      problemDescriptors.foreach(problem => {
        val maybeAnnotation = problem.message match {
          case Some(message) =>
            Some(annotations.createWarningAnnotation(problem.element.getContext, message))
          case None if annotations.isBatchMode =>
            None // skip annotation when there are only quick fixes without message in batch mode
          case None =>
            Some(annotations.createAnnotation(suggestionHighlightSeverity, problem.element.getContext.getTextRange, null))
        }

        maybeAnnotation.foreach(annotation => problem.quickFixes.foreach(fix => annotation.registerFix(fix)))
      })
    }
  }

  private def detectProblemsInVersion(pkg: String, version: String, element: PsiElement): Seq[QuickFixGroup] = {
    val versionConstraint = parseVersion(version)

    detectUnboundedVersionProblem(versionConstraint, pkg, element) ++ 
      detectWildcardAndOperatorCombo(versionConstraint, pkg, element) ++
      detectEquivalents(versionConstraint, pkg, element)
      
  }

  private def detectUnboundedVersionProblem(version: Option[Constraint], packageName: String, element: PsiElement): Seq[QuickFixGroup] = {
    version
      .filter(!_.isBounded)
      .map(versionConstraint => {
        val installedPackages = InstalledPackages.forFile(element.getContainingFile.getVirtualFile)
        def createChangeVersionQuickFixes(jsonObject: JsonObject): List[IntentionAction] = {
          installedPackages.get(packageName).toList
            .filter(_.replacedBy.isEmpty)
            .map(_.version)
            .flatMap(VersionSuggestions.suggestionsForVersion(_, "", mostSignificantFirst = false))
            .zipWithIndex
            .map { case(version, priority) =>
              val message = ComposerBundle.message("inspection.quickfix.setPackageVersion", version)
              changePackageVersionQuickFix(packageName, version, jsonObject, message, Some(priority))
            }
        }

        (
          Some(ComposerBundle.message("inspection.version.unboundVersion")),
          createQuickFixes(element, createChangeVersionQuickFixes) ++
            versionQuickFixes(getUnboundVersionFixers)(packageName, versionConstraint, element) ++
            List(new ExcludePatternAction(packageName)) ++
            packageVendorPattern(packageName).map(new ExcludePatternAction(_)).toList
        )
      })
      .toList
  }

  private def versionQuickFixes(fixers: Seq[Constraint => Option[Constraint]])(
    pkg: String,
    version: Constraint,
    element: PsiElement
  ): Seq[IntentionAction] = {
    def create(jsonObject: JsonObject) = {
      fixers
        .map(version.replace)
        .filter(_ != version)
        .map(fixedVersion => changePackageVersionQuickFix(pkg, fixedVersion.presentation, jsonObject))
    }

    createQuickFixes(element, create)
  }

  private def createQuickFixes(element: PsiElement, createFix: JsonObject => Seq[IntentionAction]): Seq[IntentionAction] = {
    for {
      property <- ensureJsonProperty(element.getParent).toList
      jsonObject <- ensureJsonObject(property.getParent).toList
      fix <- createFix(jsonObject)
    } yield fix
  }

  private def getUnboundVersionFixers: Seq[Constraint => Option[Constraint]] = List(ConstraintOperator.~, ConstraintOperator.^).flatMap(operator => {
    List(
      (c: Constraint) => c match {
        case OperatorConstraint(ConstraintOperator.>=, constraint, separator) => Some(OperatorConstraint(operator, constraint, separator))
        case _ => None
      },
      (c: Constraint) => c match {
        case OperatorConstraint(ConstraintOperator.>, constraint, separator) => Some(OperatorConstraint(operator, constraint.replace {
          case SemanticConstraint(version) => Some(SemanticConstraint(version.incrementLast))
          case _ => None
        }, separator))
        case _ => None
      }
    )
  })

  private def detectWildcardAndOperatorCombo(version: Option[Constraint], pkg: String, element: PsiElement): Seq[QuickFixGroup] = {
    version
      .filter(_ contains wildcardAndOperatorCombination)
      .map(versionConstraint => (
        Some(ComposerBundle.message("inspection.version.wildcardAndComparison")),
        versionQuickFixes(getWildcardAndOperatorComboFixers)(pkg, versionConstraint, element)
      ))
      .toList
  }

  private def wildcardAndOperatorCombination(constraint: Constraint) = constraint match {
    case OperatorConstraint(_, WildcardConstraint(_), _) => true
    case OperatorConstraint(_, WrappedConstraint(WildcardConstraint(_), _, _), _) => true
    case _ => false
  }

  private def getWildcardAndOperatorComboFixers: Seq[Constraint => Option[Constraint]] = {
    List(
      (c: Constraint) => c match {
        case OperatorConstraint(operator, WildcardConstraint(Some(constraint)), separator) => {
          Some(OperatorConstraint(operator, constraint, separator))
        }
        case _ => None
      },
      (c: Constraint) => c match {
        case OperatorConstraint(operator, WrappedConstraint(WildcardConstraint(Some(constraint)), prefix, suffix), separator) => {
          Some(OperatorConstraint(operator, WrappedConstraint(constraint, prefix, suffix), separator))
        }
        case _ => None
      }
    )
  }

  def detectEquivalents(version: Option[Constraint], pkg: String, element: PsiElement): Seq[QuickFixGroup] = {
    version
      .toList.view
      .flatMap(VersionEquivalents.equivalentsFor)
      .map(equivalentVersion => createQuickFixes(element, jsonObject => List(changeEquivalentPackageVersionQuickFix(pkg, equivalentVersion, jsonObject))))
      .map(quickFix => (None, quickFix))
  }

  private def changePackageVersionQuickFix(packageName: String, fixedVersion: String, jsonObject: JsonObject): IntentionAction = {
    changePackageVersionQuickFix(packageName, fixedVersion, jsonObject, ComposerBundle.message("inspection.quickfix.setPackageVersion", fixedVersion))
  }

  private def changeEquivalentPackageVersionQuickFix(pkg: String, fixedVersion: Constraint, jsonObject: JsonObject): IntentionAction = {
    changePackageVersionQuickFix(pkg, fixedVersion.presentation, jsonObject, ComposerBundle.message("inspection.quickfix.setPackageEquivalentVersion", fixedVersion.presentation))
  }

  private def changePackageVersionQuickFix(packageName: String, newVersion: String, jsonObject: JsonObject, message: String, maybePriority: Option[Int] = None): IntentionAction = {
    val quickFix = new SetPropertyValueQuickFix(jsonObject, packageName, SString(), newVersion) {
      override def getText: String = message
    }
    maybePriority match {
      case Some(priority) =>
        new QuickFixIntentionActionAdapterWithPriority(quickFix, priority)
      case None =>
        new QuickFixIntentionActionAdapter(quickFix)
    }

  }

  private def packageVendorPattern(pkg: String): Option[String] = pkg.split('/').headOption.map(_ + "/*")

  private def excluded(project: Project): StringPattern = {
    string().`with`(new PatternCondition[String]("matches") {
      override def accepts(t: String, context: ProcessingContext): Boolean = {
        ProjectSettings(project).getUnboundedVersionInspectionSettings.isExcluded(t)
      }
    })
  }
}

private object PackageVersionAnnotator {
  import org.psliwa.idea.composerJson.util.Funcs._
  val parseVersion: (String) => Option[Constraint] = memorize(40)(Parser.parse)
  val pattern = packageElement.afterLeaf(":")
  val `HighlightSeverity.SUGGESTION` =
    new HighlightSeverity(HighlightSeverity.INFORMATION.myName, HighlightSeverity.WEAK_WARNING.myVal - 2)
}
