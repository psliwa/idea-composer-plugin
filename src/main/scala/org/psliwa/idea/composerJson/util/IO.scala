package org.psliwa.idea.composerJson.util

import scala.io.Source

object IO {
  def loadUrl(uri: String): Either[String,String] = {
    try {
      val in = Source.fromURL(uri)
      try {
        Right(in.getLines().mkString)
      } finally {
        in.close()
      }
    } catch {
      case e: Throwable => Left(e.getStackTraceString)
    }
  }
}
