package org.psliwa.idea.composer.idea.completionContributor

import org.psliwa.idea.composer.idea.BaseLookupElement

class PackagesCompletionTest extends TestCase {
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

    val pkg = "ps/image-optimizer"
    contributor.setPackagesLoader(() => List(BaseLookupElement(pkg)))

    completion(
      """
        |{
        | "require": {
        |   ps/image-opti<caret>
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
}