package org.psliwa.idea.composerJson.intellij.codeAssist.php

import org.psliwa.idea.composerJson.intellij.codeAssist.InspectionTest

class PhpInspectionTest extends InspectionTest {
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
        |    public abstract static function abstractHook(\Composer\Script\Event $event);
        |    public static function clearCache2(\Composer\Script\Event $event) {}
        |    public static function clearCache3(\Composer\Installer\PackageEvent $event) {}
        |    private static function clearCachePrivate(\Composer\Script\Event $event) {}
        |    public function clearCacheNonStatic(\Composer\Script\Event $event) {}
        |    public static function invalidArgTypeHook(\stdClass $object) {}
        |    public static function tooManyArgsHook(\Composer\Script\Event $event, $someArg) {}
        |    public static function optionalExtraArgHook(\Composer\Script\Event $event, $someArg = null) {}
        |    public static function withoutTypeHintHook($event) {}
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

  def testCallbackValidity_givenCallbackIsValidHook_isShouldNotBeReported(): Unit = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "ComposerJson\\Example1\\ScriptHandler::clearCache1"
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_givenCallbackAcceptsInstallerPackageEvent_isShouldNotBeReported(): Unit = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "ComposerJson\\Example1\\ScriptHandler::clearCache3"
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_givenScriptsAsArray_givenCallbackIsValidHook_isShouldNotBeReported(): Unit = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": [ "ComposerJson\\Example1\\ScriptHandler::clearCache1" ]
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_givenMethodDoesNotExist_isShouldBeReported(): Unit = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "ComposerJson\\Example1\\ScriptHandler::<warning>unexistingMethod</warning>"
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity__givenScriptAsArray_givenMethodDoesNotExist_isShouldBeReported(): Unit = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": [ "ComposerJson\\Example1\\ScriptHandler::<warning>unexistingMethod</warning>" ]
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_givenClassDoesNotExist_isShouldBeReported(): Unit = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "<warning>ComposerJson\\Example1\\UnexistingClass::clearCache1</warning>"
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_givenMethodIsPrivate_isShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "ComposerJson\\Example1\\ScriptHandler::<warning>clearCachePrivate</warning>"
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_givenMethodIsNotStatic_isShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "ComposerJson\\Example1\\ScriptHandler::<warning>clearCacheNonStatic</warning>"
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_givenMethodHasInvalidArgumentType_isShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "ComposerJson\\Example1\\ScriptHandler::<warning>invalidArgTypeHook</warning>"
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_givenMethodHasTooManyArgs_isShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "ComposerJson\\Example1\\ScriptHandler::<warning>tooManyArgsHook</warning>"
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_givenMethodOptionalExtraArg_isShouldNotBeReported() = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "ComposerJson\\Example1\\ScriptHandler::optionalExtraArgHook"
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_givenAbstractMethod_isShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "ComposerJson\\Example1\\ScriptHandler::<warning>abstractHook</warning>"
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_givenMethodWithoutEventTypeHint_isShouldNotBeReported() = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "ComposerJson\\Example1\\ScriptHandler::withoutTypeHintHook"
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_givenCallbackIsACommand_isShouldNotBeReported() = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "ls -l"
        |  }
        |}
      """.stripMargin)
  }

  def testCallbackValidity_methodIsEmpty_isShouldBeReported() = {
    checkInspection(
      """
        |{
        |  "scripts": {
        |    "post-create-project-cmd": "<warning>ComposerJson\\Example1\\ScriptHandler::</warning>"
        |  }
        |}
      """.stripMargin)
  }
}
