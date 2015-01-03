package org.psliwa.idea.composerJson.inspection

import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.inspection.problem.ProblemDescriptor
import com.intellij.patterns.PlatformPatterns._

class PackageVersionAnnotator extends Annotator {

  val pattern = psiElement(classOf[JsonStringLiteral])
    .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
    .withLanguage(JsonLanguage.INSTANCE)
    .afterLeaf(":")
    .withParent(
      psiElement(classOf[JsonProperty]).withParent(
        psiElement(classOf[JsonObject]).withParent(
          psiElement(classOf[JsonProperty]).withName("require", "require-dev")
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
    List()
//    List(ComposerBundle.message("inspection.version.unboundVersion"))
  }

  private def getStringValue(value: PsiElement): Option[String] = {
    import PsiExtractors.JsonStringLiteral

    value match {
      case JsonStringLiteral(x) => Some(x)
      case _ => None
    }
  }
}
