package org.psliwa.idea.composerJson.composer.model.version

import org.junit.Assert._
import org.junit.Test

//those tests depends on version.ParserTest
class VersionEquivalentsTest {

  @Test
  def simpleSemanticVersionShouldNotHasEquivalents(): Unit = {
    assertEquals(Nil, equivalents("1.2.3"))
  }

  @Test
  def nsrTildeOperatorShouldHasRangeEquivalent(): Unit = {
    assertEquals(List(">=1.2.3 <1.3.0"), equivalents("~1.2.3"))
    assertEquals(List(">=1.2 <2.0.0"), equivalents("~1.2"))
    assertEquals(List(">=1.0 <2.0.0"), equivalents("~1"))
  }

  @Test
  def nsrDashOperatorShouldHasRangeEquivalent(): Unit = {
    assertEquals(List(">=1.2.3 <2.0.0"), equivalents("^1.2.3"))
    assertEquals(List(">=1.2 <2.0.0"), equivalents("^1.2"))
  }

  @Test
  def givenPreReleaseSemanticVersion_nsrDashOperatorShouldHasRangeEquivalent(): Unit = {
    assertEquals(List(">=0.2.3 <0.3.0"), equivalents("^0.2.3"))
    assertEquals(List(">=0.2 <0.3.0"), equivalents("^0.2"))
  }

  @Test
  def nsrRangeShouldHasNsrDashOperatorEquivalent(): Unit = {
    assertEquals(List("^1.2.1"), equivalents(">=1.2.1,<2.0.0"))
  }

  private def equivalents(s: String): List[String] = {
    val constraint = Parser.parse(s).get
    VersionEquivalents.equivalentsFor(constraint).map(_.presentation).toList
  }

  @Test
  def nsrRangeShouldHasNsrOperatorEquivalent(): Unit = {
    assertEquals(List("~1.2.3"), equivalents(">=1.2.3,<1.3.0"))
    assertEquals(List("~1.2.0"), equivalents(">=1.2,<1.3"))
    assertEquals(List("~1.2"), equivalents(">=1.2,<2.0.0"))
    assertEquals(List("~1.2"), equivalents(">=1.2.0,<2.0.0"))
  }

  @Test
  def nonNsrRangeShouldNotHasEquivalents(): Unit = {
    List(">=1.2.3,<1.3.3", ">=1.2.3,<1.6.0", ">1.2.3,<1.3.3").foreach(version => {
      assertEquals(Nil, equivalents(version))
    })
  }
}
