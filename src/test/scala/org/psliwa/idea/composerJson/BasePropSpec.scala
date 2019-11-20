package org.psliwa.idea.composerJson

import org.scalacheck.Prop
import org.scalatest.PropSpec
import org.scalatestplus.scalacheck.Checkers

abstract class BasePropSpec extends PropSpec with Checkers {
  protected def property(testName: String)(testFun: => Prop, params: PropertyCheckConfigParam*): Unit = {
    super.property(testName)(check(testFun, params: _*))
  }
}
