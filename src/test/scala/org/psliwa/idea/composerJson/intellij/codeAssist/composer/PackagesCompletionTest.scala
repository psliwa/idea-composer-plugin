package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import org.psliwa.idea.composerJson.intellij.codeAssist.BaseLookupElement

class PackagesCompletionTest extends AbstractPackagesTest {
  def testPackageNameCompletion_versionQuotesShouldBeFixed() = {
    val contributor = getCompletionContributor

    val pkg = "ps/image-optimizer"
    contributor.setPackagesLoader(() => List(BaseLookupElement(pkg)))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-opti<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "<caret>"
        | }
        |}
      """.stripMargin
    )
  }

  def testPackageNameCompletion_startTypingWithoutQuotes_quotesShouldBeFixed() = {
    val contributor = getCompletionContributor

    val pkg = "symfony/symfony"
    contributor.setPackagesLoader(() => List(BaseLookupElement(pkg)))

    completion(
      """
        |{
        | "require": {
        |   symsym<caret>
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "symfony/symfony": "<caret>"
        | }
        |}
      """.stripMargin
    )
  }

  def testVersionCompletion_givenPrefix_quotesShouldBeStillValid() = {
    val pkg = "ps/image-optimizer"

    setCompletionPackageLoader(() => List(BaseLookupElement(pkg)))
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "~1<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "~1.2<caret>"
        | }
        |}
      """.stripMargin
    )
  }

  def testVersionCompletion_givenPrefixWithSpace_completeVersion() = {
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2 123<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2 1.2.3"
        | }
        |}
      """.stripMargin
    )
  }

  def testCompletionPackageVersion_givenPrefixWithSpaceAndTilda_completeVersion() = {
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2 ~12<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2 ~1.2"
        | }
        |}
      """.stripMargin
    )
  }

  def testVersionCompletion_givenPrefixWithComma_completeVersion() = {
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2,123<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2,1.2.3"
        | }
        |}
      """.stripMargin
    )
  }

  def testVersionCompletion_givenComparisonCharsRange_completeVersion() = {
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": ">=1.2.2,<123<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": ">=1.2.2,<1.2.3"
        | }
        |}
      """.stripMargin
    )
  }

  def testVersionCompletion_givenVersionRange_completeFirstMember() = {
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": ">=123<caret>,<1.2.4"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": ">=1.2.3,<1.2.4"
        | }
        |}
      """.stripMargin
    )
  }

  def testVersionCompletion_givenStability_completeStability() = {
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.*@d<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.*@dev<caret>"
        | }
        |}
      """.stripMargin
    )
  }

  def testVersionCompletion_givenRCStability_completeStability() = {
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.*@R<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.*@RC<caret>"
        | }
        |}
      """.stripMargin
    )
  }
}