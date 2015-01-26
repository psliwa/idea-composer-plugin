package org.psliwa.idea.composerJson.intellij.codeAssist.php

import com.intellij.psi.PsiPolyVariantReferenceBase
import com.jetbrains.php.lang.psi.elements.PhpNamedElement
import org.junit.Assert._
import org.psliwa.idea.composerJson._
import org.psliwa.idea.composerJson.intellij.codeAssist.CompletionTest

class PhpReferenceTest extends CompletionTest {

  private val ComposerScriptEvent = "\\Composer\\Script\\Event"

  override def setUp(): Unit = {
    super.setUp()

    myFixture.configureByText("classes.php",
      """
        |<?php
        |
        |namespace ComposerJson\Example1 {
        |  class ClassWithoutStaticMembers {
        |    public function method1(){}
        |    public function method2($arg1){}
        |  }
        |
        |  class ScriptHandler {
        |    public static function clearCache1(\Composer\Script\Event $event) {}
        |    public static function clearCache2(\Composer\Script\Event $event) {}
        |    private static function clearCachePrivate(\Composer\Script\Event $event) {}
        |    public function clearCacheNonStatic(\Composer\Script\Event $event) {}
        |    public static function invalidHook(\stdClass $object) {}
        |  }
        |}
        |
        |namespace ComposerJson\Example2 {
        |  class ClassWithStaticMembers {
        |    public static function invalidHook($arg1, $arg2) {}
        |  }
        |}
        |
        |namespace ComposerJson2\Example3 {}
      """.stripMargin
    )
  }

  def testCallbackReference_givenExistingClassAndMethod_referenceShouldBeFound() = {
    checkPhpReference(
      """
        |{
        |  "scripts": {
        |    "pre-install-cmd": "ComposerJson\\Example1\\ScriptHandler::clearCache1<caret>"
        |  }
        |}
      """.stripMargin,
      List("ScriptHandler", "clearCache1")
    )
  }

  def testCallbackReference_givenArrayElement_givenExistingClassAndMethod_referenceShouldBeFound() = {
    checkPhpReference(
      """
        |{
        |  "scripts": {
        |    "pre-install-cmd": [ "ComposerJson\\Example1\\ScriptHandler::clearCache1<caret>" ]
        |  }
        |}
      """.stripMargin,
      List("ScriptHandler", "clearCache1")
    )
  }

  def testCallbackReference_givenExistingClass_givenUnexistingMethod_referenceOnlyToClassShouldBeFound() = {
    checkPhpReference(
      """
        |{
        |  "scripts": {
        |    "pre-install-cmd": "ComposerJson\\Example1\\ScriptHandler::unexisting<caret>"
        |  }
        |}
      """.stripMargin,
      List("ScriptHandler")
    )
  }

  def testCallbackReference_givenUnexistingClass_referenceShouldNotBeFound() = {
    checkPhpReference(
      """
        |{
        |  "scripts": {
        |    "pre-install-cmd": "Unexisting::clearCache<caret>"
        |  }
        |}
      """.stripMargin,
      List()
    )
  }

  def testCallbackReference_givenExistingClass_referenceShouldBeFound() = {
    checkPhpReference(
      """
        |{
        |  "scripts": {
        |    "pre-install-cmd": "ComposerJson\\Example1\\ClassWithoutStaticMembers<caret>"
        |  }
        |}
      """.stripMargin,
      List("ClassWithoutStaticMembers")
    )
  }

  def testNamespaceReference_givenExistingNamespace_referenceShouldBeFound() = {
    checkPhpReference(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "ComposerJson\\Example1<caret>": ""
        |    }
        |  }
        |}
      """.stripMargin,
      List("Example1")
    )
  }

  def testNamespaceReference_givenExistingNamespace_givenPropertyIsIncomplete_referenceShouldBeFound() = {
    checkPhpReference(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "ComposerJson\\Example1<caret>"
        |    }
        |  }
        |}
      """.stripMargin,
      List("Example1")
    )
  }

  def testNamespaceReference_givenExistingNamespaceAsValue_referenceShouldNotBeFound() = {
    checkPhpReference(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "": "ComposerJson\\Example1<caret>"
        |    }
        |  }
        |}
      """.stripMargin,
      List()
    )
  }

  def testCallbackSuggestion_givenClassPrefix_suggestMatchingClasses() = {
    suggestions(
      """
        |{
        |  "scripts": {
        |    "pre-install-cmd": "SH<caret>"
        |  }
        |}
      """.stripMargin,
      Array("ScriptHandler"),
      Array("ClassWithoutStaticMembers")
    )
  }

  def testCallbackSuggestion_givenMethodPrefix_suggestMatchingMethods() = {
    val className = "ComposerJson\\\\Example1\\\\ScriptHandler"
    suggestions(
      """
        |{
        |  "scripts": {
        |    "pre-install-cmd": "ComposerJson\\Example1\\ScriptHandler::c<caret>"
        |  }
        |}
      """.stripMargin,
      Array(s"$className::clearCache1", s"$className::clearCache2"),
      Array(s"$className::clearCachePrivate", s"$className::clearCacheNonStatic")
    )
  }

  def testNamespaceSuggestion_givenEmptyPrefix_suggestTopLevelNamespaces() = {
    suggestions(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "<caret>": ""
        |    }
        |  }
        |}
      """.stripMargin,
      Array("ComposerJson"),
      Array("ComposerJson\\Example1", "Example1")
    )
  }

  def testNamespaceSuggestion_givenPrefix_suggestTopLevelNamespaces() = {
    suggestions(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "CJ<caret>": ""
        |    }
        |  }
        |}
      """.stripMargin,
      Array("ComposerJson"),
      Array("ComposerJson\\Example1", "Example1")
    )
  }

  def testNamespaceSuggestion_givenTopLevelNamespace_suggestChildNamespaces() = {
    suggestions(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "ComposerJson\\<caret>": ""
        |    }
        |  }
        |}
      """.stripMargin,
      Array("ComposerJson\\\\Example1"),
      Array("Example1", "ComposerJson", "ComposerJson2\\\\Example3")
    )
  }

  def testCallbackCompletion_givenPrefix_slashesShouldBeEscaped() = {
    completion(
      """
        |{
        |  "scripts": {
        |    "pre-install-cmd": "ScrHa<caret>"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "scripts": {
        |    "pre-install-cmd": "ComposerJson\\Example1\\ScriptHandler::<caret>"
        |  }
        |}
      """.stripMargin
    )
  }

  def testCallbackCompletion_givenClassAndMethodPrefix_methodShouldBeCompleted() = {
    completion(
      """
        |{
        |  "scripts": {
        |    "pre-install-cmd": "ComposerJson\\Example1\\ScriptHandler::cC1<caret>"
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "scripts": {
        |    "pre-install-cmd": "ComposerJson\\Example1\\ScriptHandler::clearCache1<caret>"
        |  }
        |}
      """.stripMargin
    )
  }

  def testNamespaceCompletion_givenSecondLevelNamespacePrefix_slashesShouldBeEscaped() = {
    completion(
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "ComposerJson\\E1<caret>": ""
        |    }
        |  }
        |}
      """.stripMargin,
      """
        |{
        |  "autoload": {
        |    "psr-0": {
        |      "ComposerJson\\Example1<caret>": ""
        |    }
        |  }
        |}
      """.stripMargin
    )
  }

  private def checkPhpReference(content: String, expectedReferences: List[String]): Unit = {
    assertEquals(expectedReferences, getPhpReference(content))
  }

  private def getPhpReference(content: String): List[String] = {
    myFixture.configureByText(ComposerJson, content)

    myFixture.getProject.getBaseDir.refresh(false, true)

    val element = myFixture.getFile.findElementAt(myFixture.getCaretOffset).getParent

    element.getReferences
      .filter(_.isInstanceOf[PsiPolyVariantReferenceBase[_]])
      .map(_.asInstanceOf[PsiPolyVariantReferenceBase[_]])
      .flatMap(_.multiResolve(false))
      .map(_.getElement.asInstanceOf[PhpNamedElement].getName)
      .toList
  }
}
