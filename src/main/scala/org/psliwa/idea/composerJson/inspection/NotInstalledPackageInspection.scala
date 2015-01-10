package org.psliwa.idea.composerJson.inspection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.ComposerBundle
import org.psliwa.idea.composerJson.composer.{Packages, InstalledPackages}
import org.psliwa.idea.composerJson.inspection.problem.ProblemDescriptor
import org.psliwa.idea.composerJson.json.Schema
import org.psliwa.idea.composerJson.util.PsiElements._

class NotInstalledPackageInspection extends Inspection {

  override protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit = {
    import scala.collection.JavaConversions._

    val installedPackages = InstalledPackages.forFile(element.getContainingFile.getVirtualFile)

    val problemDescriptors = for {
      jsonObject <- ensureJsonObject(element).toList
      propertyName <- List("require", "require-dev")
      property <- Option(jsonObject.findProperty(propertyName)).toList
      packagesObject <- Option(property.getValue).toList
      packagesObject <- ensureJsonObject(packagesObject).toList
      packageProperty <- packagesObject.getPropertyList
      problem <- detectProblems(packageProperty, installedPackages)
    } yield problem

    problemDescriptors.foreach(problem => problems.registerProblem(problem.element, problem.message))
  }

  private def detectProblems(property: JsonProperty, installedPackages: Packages): List[ProblemDescriptor[_]] = {
    if(property.getName.contains("/") && !getPackageVersion(property).isEmpty && !installedPackages.contains(property.getName)) {
      List(
        ProblemDescriptor(property, ComposerBundle.message("inspection.notIninstalledPackage.packageIsNotInstalled", property.getName))
      )
    } else {
      List()
    }
  }

  private def getPackageVersion(property: JsonProperty): String = {
    import scala.collection.JavaConversions._

    val maybeVersion = for {
      value <- Option(property.getValue)
      stringLiteral <- ensureJsonStringLiteral(value)
    } yield stringLiteral.getTextFragments.foldLeft("")(_ + _.second)

    maybeVersion.getOrElse("")
  }
}
