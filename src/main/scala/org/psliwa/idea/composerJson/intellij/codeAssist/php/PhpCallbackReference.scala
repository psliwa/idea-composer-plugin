package org.psliwa.idea.composerJson.intellij.codeAssist.php

import java.util

import com.intellij.codeInsight.completion.impl.CamelHumpMatcher
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.stubs.StubIndexKey
import com.intellij.psi.{PsiElementResolveResult, PsiPolyVariantReferenceBase, ResolveResult}
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.completion.{PhpClassLookupElement, PhpLookupElement}
import com.jetbrains.php.lang.psi.elements.Method
import org.psliwa.idea.composerJson.intellij.codeAssist.AutoPopupInsertHandler
import scala.jdk.CollectionConverters._
import PhpCallbackReference._
import PhpUtils._

private class PhpCallbackReference(element: JsonStringLiteral)
    extends PsiPolyVariantReferenceBase[JsonStringLiteral](element) {
  private val referenceName: String = getFixedReferenceName(element.getText)
  private val methodExists = referenceName.indexOf("::") > 0
  private val (className, methodName) = getCallableInfo(referenceName)

  override def multiResolve(incompleteCode: Boolean): Array[ResolveResult] = {
    val phpIndex = PhpIndex.getInstance(element.getProject)

    val classes = phpIndex.getClassesByFQN(className).asScala
    val classResult = classes
      .map(new PsiElementResolveResult(_))

    val methodResult = if (methodExists) {
      classes
        .flatMap(cls => Option(cls.findMethodByName(methodName)).toList)
        .map(new PsiElementResolveResult(_))
    } else {
      Seq()
    }

    (classResult ++ methodResult).toArray
  }

  override def getVariants: Array[AnyRef] = {

    val phpIndex = PhpIndex.getInstance(element.getProject)

    val results = if (methodExists) {
      val methodMatcher = new CamelHumpMatcher(methodName)
      phpIndex
        .getClassesByFQN(className)
        .asScala
        .to(LazyList)
        .flatMap(cls => cls.getMethods.asScala)
        .filter(method => method.getAccess.isPublic && method.isStatic && !method.isAbstract)
        .filter(method => methodMatcher.prefixMatches(method.getName))
        .map(method => new PhpMethodLookupElement(method))
    } else {
      val classMatcher = new CamelHumpMatcher(className)
      phpIndex
        .getAllClassNames(classMatcher)
        .asScala
        .to(LazyList)
        .flatMap(className => Option(phpIndex.getClassByName(className)).toList)
        .filter(cls => !cls.isAbstract && !cls.isInterface && !cls.isInterface)
        .filter(_.hasStaticMembers)
        //        TODO: filtering classes by methods that have composer event is not efficient - maybe indexing will be a solution?
        //        .filter(cls => exists(cls.getMethods, hookMethod))
        .map(cls => new PhpClassLookupElement(cls, true, new AutoPopupInsertHandler(PhpClassInsertHandler)))
    }

    results.toArray
  }

}

private object PhpCallbackReference {
  lazy val MethodStubIndexKey: StubIndexKey[Nothing, Nothing] =
    StubIndexKey.createIndexKey("org.psliwa.idea.composerJson.phpMethod")
  private[php] val ComposerEventTypes = List(
    "\\Composer\\EventDispatcher\\Event",
    "\\Composer\\Script\\Event",
    "\\Composer\\Script\\PackageEvent",
    "\\Composer\\Installer\\PackageEvent",
    "\\Composer\\Installer\\InstallerEvent ",
    "\\Composer\\Script\\CommandEvent",
    "\\Composer\\Plugin\\CommandEvent",
    "\\Composer\\Plugin\\PreFileDownloadEvent"
  )

  private[this] def getMethodFQN(method: Method) = {
    getFixedFQN(method.getContainingClass) + "::" + method.getName
  }

  private class PhpMethodLookupElement(method: Method)
      extends PhpLookupElement(
        getMethodFQN(method),
        MethodStubIndexKey,
        method.getIcon,
        null,
        method.getProject,
        null
      ) {
    override def renderElement(presentation: LookupElementPresentation): Unit = {
      super.renderElement(presentation)
      presentation.setItemText(
        method.getName + "(" + method.getParameters.map(_.getType.toStringResolved).mkString(", ") + ")"
      )
    }
  }
}
