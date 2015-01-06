package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.openapi.project.Project
import com.intellij.patterns.{PatternCondition, StringPattern}
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.composer.version._
import org.psliwa.idea.composerJson.inspection.problem.ProblemDescriptor
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.StandardPatterns._
import org.psliwa.idea.composerJson.intellij.Patterns._
import org.psliwa.idea.composerJson.json.SString
import org.psliwa.idea.composerJson.settings.ComposerJsonSettings

class PackageVersionAnnotator extends Annotator {
  import PackageVersionAnnotator._

  override def annotate(element: PsiElement, annotations: AnnotationHolder): Unit = {
    val pattern = psiElement().and(PackageVersionAnnotator.pattern)
      .withParent(psiElement().withName(and(stringContains("/"), not(excluded(element.getProject)))))

    if(pattern.accepts(element)) {
      val problemDescriptors = for {
        version <- getStringValue(element).toList
        pkg <- ensureJsonProperty(element.getParent).map(_.getName).toList
        problem <- detectProblemsInVersion(pkg, version, element)
      } yield ProblemDescriptor(element, problem._1, problem._2)

      problemDescriptors.foreach(problem => {
        val annotation = annotations.createWarningAnnotation(problem.element.getContext, problem.message)
        problem.quickFixes.foreach(fix => annotation.registerFix(fix))
      })
    }
  }

  private def ensureJsonProperty(element: PsiElement): Option[JsonProperty] = element match {
    case x: JsonProperty => Some(x)
    case _ => None
  }

  private def ensureJsonObject(element: PsiElement): Option[JsonObject] = element match {
    case x: JsonObject => Some(x)
    case _ => None
  }

  private def detectProblemsInVersion(pkg: String, version: String, element: PsiElement): Seq[(String, Seq[IntentionAction])] = {
    parseVersion(version)
      .filter(!_.isBounded)
      .map(versionConstraint => (
        ComposerBundle.message("inspection.version.unboundVersion"),
        fixUnboundVersion(pkg, versionConstraint, element) ++ List(new ExcludePatternAction(pkg)) ++
          packageVendorPattern(pkg).map(new ExcludePatternAction(_)).toList
      ))
      .toList
  }

  private def fixUnboundVersion(pkg: String, version: Constraint, element: PsiElement): Seq[IntentionAction] = {
    for {
      property <- ensureJsonProperty(element.getParent).toList
      jsonObject <- ensureJsonObject(property.getParent).toList
      fix <- fixUnboundVersion(pkg, version, jsonObject)
    } yield fix
  }

  private def fixUnboundVersion(pkg: String, version: Constraint, jsonObject: JsonObject): Seq[IntentionAction] = {
    getUnboundVersionFixers
      .map(version.replace)
      .filter(_ != version)
      .map(fixedVersion => changePackageVersionQuickFix(pkg, fixedVersion, jsonObject))
  }

  private def getUnboundVersionFixers: Seq[Constraint => Option[Constraint]] = List(ConstraintOperator.~, ConstraintOperator.^).flatMap(operator => {
    List(
      (c: Constraint) => c match {
        case OperatorConstraint(ConstraintOperator.>=, constraint) => Some(OperatorConstraint(operator, constraint))
        case _ => None
      },
      (c: Constraint) => c match {
        case OperatorConstraint(ConstraintOperator.>, constraint) => Some(OperatorConstraint(operator, constraint.replace {
          case SemanticConstraint(version) => Some(SemanticConstraint(version.incrementLast))
          case _ => None
        }))
        case _ => None
      }
    )
  })

  private def changePackageVersionQuickFix(pkg: String, fixedVersion: Constraint, jsonObject: JsonObject): IntentionAction = {
    new QuickFixIntentionActionAdapter(new SetPropertyValueQuickFix(jsonObject, pkg, SString(), fixedVersion.toString) {
      override def getText: String = ComposerBundle.message("inspection.quickfix.setPackageVersion", fixedVersion.toString)
    })
  }

  private def packageVendorPattern(pkg: String): Option[String] = pkg.split('/').headOption.map(_ + "/*")

  private def getStringValue(value: PsiElement): Option[String] = {
    import PsiExtractors.JsonStringLiteral

    value match {
      case JsonStringLiteral(x) => Some(x)
      case _ => None
    }
  }

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
  val pattern = psiElement(classOf[JsonStringLiteral])
    .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
    .withLanguage(JsonLanguage.INSTANCE)
    .afterLeaf(":")
    .withParent(
      psiElement(classOf[JsonProperty]).withParent(
        psiElement(classOf[JsonObject]).withParent(
          psiElement(classOf[JsonProperty]).withName("require")
        )
      )
    )
}
