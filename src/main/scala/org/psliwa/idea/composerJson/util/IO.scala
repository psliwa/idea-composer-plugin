package org.psliwa.idea.composerJson.util

import scala.io.Source
import scala.util.Try

object IO {
  def loadUrl(uri: String): Try[String] = {
    Try {
      val in = Source.fromURL(uri)
      try {
        in.getLines().mkString
      } finally {
        in.close()
      }
    }
  }
}
