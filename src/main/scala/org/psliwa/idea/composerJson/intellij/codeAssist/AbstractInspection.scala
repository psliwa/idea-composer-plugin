package org.psliwa.idea.composerJson.intellij.codeAssist

import com.intellij.codeInspection.{InspectionManager, LocalInspectionTool, ProblemDescriptor, ProblemsHolder}
import com.intellij.psi.{PsiElement, PsiFile}
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.intellij.PsiElements
import org.psliwa.idea.composerJson.json.Schema
import PsiElements._

abstract class AbstractInspection extends LocalInspectionTool {
  val schema = ComposerSchema

  override final def checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    if(file.getName != ComposerJson) Array()
    else doCheckFile(file, manager, isOnTheFly)
  }

  private def doCheckFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array[ProblemDescriptor] = {
    val problems = new ProblemsHolder(manager, file, isOnTheFly)

    for {
      file <- ensureJsonFile(file)
      schema <- schema
      topLevelValue <- Option(file.getTopLevelValue)
    } yield collectProblems(topLevelValue, schema, problems)

    problems.getResultsArray
  }

  protected def collectProblems(element: PsiElement, schema: Schema, problems: ProblemsHolder): Unit
}
