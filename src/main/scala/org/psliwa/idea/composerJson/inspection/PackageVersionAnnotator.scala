package org.psliwa.idea.composerJson.inspection

import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.composer.version.{Constraint, Parser}
import org.psliwa.idea.composerJson.inspection.problem.ProblemDescriptor
import com.intellij.patterns.PlatformPatterns._
import org.psliwa.idea.composerJson.intellij.Patterns._

class PackageVersionAnnotator extends Annotator {

  val pattern = psiElement(classOf[JsonStringLiteral])
    .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
    .withLanguage(JsonLanguage.INSTANCE)
    .afterLeaf(":")
    .withParent(
      psiElement(classOf[JsonProperty]).withName(stringContains("/")).withParent(
        psiElement(classOf[JsonObject]).withParent(
          psiElement(classOf[JsonProperty]).withName("require")
        )
      )
    )

  override def annotate(element: PsiElement, annotations: AnnotationHolder): Unit = {
    if(pattern.accepts(element)) {
      val problemDescriptors = for {
        version <- getStringValue(element).toList
        problem <- detectProblemsInVersion(version)
      } yield ProblemDescriptor(element, problem, List())

      problemDescriptors.foreach(problem => {
        val annotation = annotations.createWarningAnnotation(problem.element.getContext, problem.message)
        problem.quickFixes.foreach(fix => annotation.registerFix(new QuickFixIntentionActionAdapter(fix)))
      })
    }
  }

  private def detectProblemsInVersion(version: String): Seq[String] = {
    import PackageVersionAnnotator._

    parseVersion(version)
      .filter(!_.isBounded)
      .map(_ => ComposerBundle.message("inspection.version.unboundVersion"))
      .toList
  }

  private def getStringValue(value: PsiElement): Option[String] = {
    import PsiExtractors.JsonStringLiteral

    value match {
      case JsonStringLiteral(x) => Some(x)
      case _ => None
    }
  }
}

private object PackageVersionAnnotator {
  import org.psliwa.idea.composerJson.util.Funcs._
  val parseVersion: (String) => Option[Constraint] = memorize(40)(Parser.parse)
}
