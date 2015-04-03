package org.psliwa.idea.composerJson.intellij.codeAssist.php

import java.util

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.json.JsonLanguage
import com.intellij.json.psi._
import com.intellij.lang.annotation.{AnnotationHolder, Annotator}
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PlatformPatterns._
import com.intellij.psi.PsiElement
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.{Method, PhpClass}
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.intellij.PsiElements._
import PhpUtils._
import org.psliwa.idea.composerJson.intellij.codeAssist.problem.ProblemDescriptor

import scala.collection.mutable.ListBuffer

class PhpCallbackAnnotator extends Annotator {
  private val callbackRegexp = "^(?i)[a-z0-9_\\\\]+(::[a-z0-9_]*)?$".r

  override def annotate(element: PsiElement, holder: AnnotationHolder): Unit = {
    if(PhpCallbackAnnotator.pattern.accepts(element)) {
      val phpIndex = PhpIndex.getInstance(element.getProject)

      for(problem <- getProblems(element, phpIndex)) {
        holder.createWarningAnnotation(problem.range, problem.message.getOrElse(""))
      }
    }
  }

  private def getProblems(element: PsiElement, phpIndex: PhpIndex): Seq[ProblemDescriptor[IntentionAction]] = {
    for {
      callback <- getStringValue(element).toList if isValidCallable(callback)
      (className, method) = getCallableInfo(callback)
      problem <- getProblems(element, phpIndex, className, method)
    } yield problem
  }

  private def isValidCallable(callback: String) = callbackRegexp.findFirstMatchIn(callback).isDefined

  private def getProblems(element: PsiElement, phpIndex: PhpIndex, className: String, methodName: String) = {
    val classes = phpIndex.getClassesByFQN(className.replace("\\\\", "\\"))
    val elementRange = element.getTextRange.grown(-2).shiftRight(1)

    val problems = ListBuffer[ProblemDescriptor[IntentionAction]]()

    if(classes.size() == 0) {
      val message = ComposerBundle.message("inspection.php.classDoesNotExist", className)
      problems += ProblemDescriptor(element, Some(message), Seq(), Some(elementRange))
    }

    if(methodName.size == 0) {
      val message = ComposerBundle.message("inspection.php.methodIsEmpty")
      problems += ProblemDescriptor(element, Some(message), Seq(), Some(elementRange))
    } else {
      problems ++= getMethodProblems(element, methodName, classes, elementRange)
    }

    problems.toList
  }

  private def getMethodProblems(element: PsiElement, methodName: String, classes: util.Collection[PhpClass], elementRange: TextRange) = {
    import scala.collection.JavaConversions._

    val problems = ListBuffer[ProblemDescriptor[IntentionAction]]()
    
    def findMethod(cls: PhpClass, methodName: String): Option[Method] = {
      Option(cls.findMethodByName(methodName))
    }

    for(cls <- classes) {
      findMethod(cls, methodName) match {
        case Some(method) => {
          if(!method.getAccess.isPublic) {
            problems += createMethodProblem(element, elementRange, methodName, "inspection.php.methodIsNotPublic")
          }

          if(!method.isStatic) {
            problems += createMethodProblem(element, elementRange, methodName, "inspection.php.methodIsNotStatic")
          }

          if(method.isAbstract) {
            problems += createMethodProblem(element, elementRange, methodName, "inspection.php.methodIsAbstract")
          }

          if(method.getParameters.length > 0) {
            val firstParameter = method.getParameters.head
            val otherParameters = method.getParameters.tail

            if(firstParameter.getType.getTypes.size() > 0 && !PhpCallbackReference.ComposerEventTypes.exists(
              firstParameter.getType.getTypes.contains(_))) {
              problems += createMethodProblem(element, elementRange, methodName, "inspection.php.invalidParameterType")
            }

            if(otherParameters.exists(!_.isOptional)) {
              problems += createMethodProblem(element, elementRange, methodName, "inspection.php.tooManyArgs")
            }
          }
        }
        case _ => {
          problems += createMethodProblem(element, elementRange, methodName, "inspection.php.methodDoesNotExist")
        }
      }
    }
    
    problems.toList
  }

  private def createMethodProblem(element: PsiElement, elementRange: TextRange, methodName: String, messageTemplate: String): ProblemDescriptor[IntentionAction] = {
    val range = new TextRange(elementRange.getEndOffset - methodName.length, elementRange.getEndOffset)
    val message = ComposerBundle.message(messageTemplate, methodName)

    ProblemDescriptor[IntentionAction](element, Some(message), Seq(), Some(range))
  }
}

private object PhpCallbackAnnotator {
  import com.intellij.patterns.StandardPatterns._

  private val scriptsPattern = psiElement(classOf[JsonProperty]).withParent(
      psiElement(classOf[JsonObject]).withParent(
      psiElement(classOf[JsonProperty]).withName("scripts")
    )
  )

  val pattern = psiElement(classOf[JsonStringLiteral])
    .inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))
    .withLanguage(JsonLanguage.INSTANCE)
    .and(
      or(
        psiElement().afterLeaf(":")
          .withParent(scriptsPattern),
        psiElement()
          .withParent(
            psiElement(classOf[JsonArray])
              .withParent(scriptsPattern)
          )
      )
    )
}
