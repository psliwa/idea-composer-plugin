package org.psliwa.idea.composer.idea

import com.intellij.codeInsight.completion.PrefixMatcher

import scala.annotation.tailrec

protected[idea] class CharContainsMatcher(prefix: String) extends PrefixMatcher(prefix) {
  override def cloneWithPrefix(prefix: String): PrefixMatcher = new CharContainsMatcher(prefix)
  override def prefixMatches(name: String): Boolean = {
    @tailrec
    def loop(prefix: String, name: String): Boolean = {
      prefix match {
        case "" => true
        case _ => {
          val index = name.indexOf(prefix.head)
          if(index >= 0) loop(prefix.tail, name.substring(index+1))
          else false
        }
      }
    }

    loop(myPrefix, name)
  }
}
