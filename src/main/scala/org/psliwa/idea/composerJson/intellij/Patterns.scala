package org.psliwa.idea.composerJson.intellij

import com.intellij.patterns.{PatternCondition, StringPattern}
import com.intellij.patterns.StandardPatterns._
import com.intellij.util.ProcessingContext

import scala.util.matching.Regex

private object Patterns {
  def stringContains(s: String): StringPattern = {
    string().`with`(new PatternCondition[String]("contains") {
      override def accepts(t: String, context: ProcessingContext): Boolean = t.contains(s)
    })
  }

  def stringMatches(r: Regex): StringPattern = {
    string().`with`(new PatternCondition[String]("matches") {
      override def accepts(t: String, context: ProcessingContext): Boolean = r.findFirstIn(t).isDefined
    })
  }
}
