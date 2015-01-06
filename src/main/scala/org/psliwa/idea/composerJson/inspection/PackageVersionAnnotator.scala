package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.openapi.project.Project
import com.intellij.patterns.{ElementPattern, PatternCondition, StringPattern}
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.composer.version.{Constraint, Parser}
import org.psliwa.idea.composerJson.inspection.problem.ProblemDescriptor
import com.intellij.patterns.PlatformPatterns._
import com.intellij.patterns.StandardPatterns._
import org.psliwa.idea.composerJson.intellij.Patterns._
import org.psliwa.idea.composerJson.settings.ComposerJsonSettings

class PackageVersionAnnotator extends Annotator {
  import PackageVersionAnnotator._

  override def annotate(element: PsiElement, annotations: AnnotationHolder): Unit = {
    val pattern = psiElement().and(PackageVersionAnnotator.pattern)
      .withParent(psiElement().withName(and(stringContains("/"), not(excluded(element.getProject)))))

    if(pattern.accepts(element)) {
      val problemDescriptors = for {
        version <- getStringValue(element).toList
        pkg <- ensureProperty(element.getParent).map(_.getName).toList
        problem <- detectProblemsInVersion(pkg, version)
      } yield ProblemDescriptor(element, problem._1, problem._2)

      problemDescriptors.foreach(problem => {
        val annotation = annotations.createWarningAnnotation(problem.element.getContext, problem.message)
        problem.quickFixes.foreach(fix => annotation.registerFix(fix))
      })
    }
  }

  private def ensureProperty(element: PsiElement): Option[JsonProperty] = element match {
    case x: JsonProperty => Some(x)
    case _ => None
  }

  private def detectProblemsInVersion(pkg: String, version: String): Seq[(String, Seq[IntentionAction])] = {
    parseVersion(version)
      .filter(!_.isBounded)
      .map(_ => (
        ComposerBundle.message("inspection.version.unboundVersion"),
        List(new ExcludePatternAction(pkg)) ++ packageVendorPattern(pkg).map(new ExcludePatternAction(_)).toList
      ))
      .toList
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
