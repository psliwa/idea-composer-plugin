package org.psliwa.idea.composerJson.util

/**
  * Generic parser library based on https://github.com/fpinscala/fpinscala/blob/master/answers/src/main/scala/fpinscala/parsing/Parsers.scala
  */
package object parsers {
  type Parser[A] = Location => Result[A]
}
