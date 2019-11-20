package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import org.psliwa.idea.composerJson.intellij.codeAssist.BaseLookupElement

class PackagesCompletionTest extends AbstractPackagesTest {
  def testPackageNameCompletion_versionQuotesShouldBeFixed(): Unit = {
    val contributor = getCompletionContributor

    val pkg = "symfony/symfony"
    contributor.setPackagesLoader(() => List(new BaseLookupElement(pkg)))

    completion(
      """
        |{
        | "require": {
        |   "symsym<caret>"
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

  def testPackageNameCompletion_startTypingWithoutQuotes_quotesShouldBeFixed(): Unit = {
    val contributor = getCompletionContributor

    val pkg = "symfony/symfony"
    contributor.setPackagesLoader(() => List(new BaseLookupElement(pkg)))

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

  def testVersionCompletion_givenPrefix_quotesShouldBeStillValid(): Unit = {
    val pkg = "ps/image-optimizer"

    setCompletionPackageLoader(() => List(new BaseLookupElement(pkg)))
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "~123<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "~1.2.3<caret>"
        | }
        |}
      """.stripMargin
    )
  }

  def testVersionCompletion_givenPrefixWithSpace_completeVersion(): Unit = {
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

  def testCompletionPackageVersion_givenPrefixWithSpaceAndTilda_completeVersion(): Unit = {
    setCompletionVersionsLoader(_ => List("1.2.3"))

    completion(
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2 ~123<caret>"
        | }
        |}
      """.stripMargin,
      """
        |{
        | "require": {
        |   "ps/image-optimizer": "1.2.2 ~1.2.3"
        | }
        |}
      """.stripMargin
    )
  }

  def testVersionCompletion_givenPrefixWithComma_completeVersion(): Unit = {
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

  def testVersionCompletion_givenComparisonCharsRange_completeVersion(): Unit = {
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

  def testVersionCompletion_givenVersionRange_completeFirstMember(): Unit = {
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

  def testVersionCompletion_givenStability_completeStability(): Unit = {
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

  def testVersionCompletion_givenRCStability_completeStability(): Unit = {
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
