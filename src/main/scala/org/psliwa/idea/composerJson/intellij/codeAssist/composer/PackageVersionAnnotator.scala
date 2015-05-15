package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.StandardPatterns._
import com.intellij.patterns.{PatternCondition, StringPattern}
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.composer.version._
import org.psliwa.idea.composerJson.intellij.codeAssist.{QuickFixIntentionActionAdapter, SetPropertyValueQuickFix}
import org.psliwa.idea.composerJson.intellij.{PsiElements, PsiExtractors}
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.ProblemDescriptor
import org.psliwa.idea.composerJson.intellij.Patterns._
import org.psliwa.idea.composerJson.json.SString
import org.psliwa.idea.composerJson.settings.ComposerJsonSettings
import PsiElements._

import scala.collection.GenTraversableOnce

class PackageVersionAnnotator extends Annotator {
  import org.psliwa.idea.composerJson.intellij.codeAssist.composer.PackageVersionAnnotator._

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
        val annotation = problem.message match {
          case Some(message) => annotations.createWarningAnnotation(problem.element.getContext, message)
          case _ => annotations.createInfoAnnotation(problem.element, null)
        }

        problem.quickFixes.foreach(fix => annotation.registerFix(fix))
      })
    }
  }

  private def detectProblemsInVersion(pkg: String, version: String, element: PsiElement): Seq[QuickFixGroup] = {
    val versionConstraint = parseVersion(version)

    detectUnboundedVersionProblem(versionConstraint, pkg, element) ++ 
      detectWildcardAndOperatorCombo(versionConstraint, pkg, element) ++
      detectEquivalents(versionConstraint, pkg, element)
      
  }

  private def detectUnboundedVersionProblem(version: Option[Constraint], pkg: String, element: PsiElement): Seq[QuickFixGroup] = {
    version
      .filter(!_.isBounded)
      .map(versionConstraint => (
        Some(ComposerBundle.message("inspection.version.unboundVersion")),
        versionQuickFixes(getUnboundVersionFixers)(pkg, versionConstraint, element) ++ List(new ExcludePatternAction(pkg)) ++
          packageVendorPattern(pkg).map(new ExcludePatternAction(_)).toList
        )
      )
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
        .map(fixedVersion => changePackageVersionQuickFix(pkg, fixedVersion, jsonObject))
    }

    createQuickFixes(element, create)
  }

  private def createQuickFixes(element: PsiElement, f: (JsonObject) => Seq[IntentionAction]): Seq[IntentionAction] = {
    for {
      property <- ensureJsonProperty(element.getParent).toList
      jsonObject <- ensureJsonObject(property.getParent).toList
      fix <- f(jsonObject)
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
      .flatMap(Version.equivalentsFor)
      .map(equivalentVersion => createQuickFixes(element, jsonObject => List(changeEquivalentPackageVersionQuickFix(pkg, equivalentVersion, jsonObject))))
      .map(quickFix => (None, quickFix))
      .toSeq
  }

  private def changePackageVersionQuickFix(pkg: String, fixedVersion: Constraint, jsonObject: JsonObject): IntentionAction = {
    changePackageVersionQuickFix(pkg, fixedVersion, jsonObject, ComposerBundle.message("inspection.quickfix.setPackageVersion", fixedVersion.presentation))
  }

  private def changeEquivalentPackageVersionQuickFix(pkg: String, fixedVersion: Constraint, jsonObject: JsonObject): IntentionAction = {
    changePackageVersionQuickFix(pkg, fixedVersion, jsonObject, ComposerBundle.message("inspection.quickfix.setPackageEquivalentVersion", fixedVersion.presentation))
  }

  private def changePackageVersionQuickFix(pkg: String, fixedVersion: Constraint, jsonObject: JsonObject, message: String): IntentionAction = {
    new QuickFixIntentionActionAdapter(new SetPropertyValueQuickFix(jsonObject, pkg, SString(), fixedVersion.presentation) {
      override def getText: String = message
    })
  }

  private def packageVendorPattern(pkg: String): Option[String] = pkg.split('/').headOption.map(_ + "/*")

  private def excluded(project: Project): StringPattern = {
    string().`with`(new PatternCondition[String]("matches") {
      override def accepts(t: String, context: ProcessingContext): Boolean = {
        ComposerJsonSettings(project).getUnboundedVersionInspectionSettings.isExcluded(t)
      }
    })
  }
}

private object PackageVersionAnnotator {
  import org.psliwa.idea.composerJson.util.Funcs._
  val parseVersion: (String) => Option[Constraint] = memorize(40)(Parser.parse)
  val pattern = packageElement.afterLeaf(":")
}
