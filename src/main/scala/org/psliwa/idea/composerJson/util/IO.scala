package org.psliwa.idea.composerJson.util

import java.net.{HttpURLConnection, URL}

import scala.io.{Codec, Source}
import scala.util.Try

object IO {
  def loadUrl(uri: String): Try[String] = {
    Try {
      val connection = new URL(uri).openConnection() match {
        case c: HttpURLConnection =>
          c.setConnectTimeout(5000)
          c.setReadTimeout(15000) // packages list might be very heavy, take it enough time to complete
          c.setRequestProperty("User-Agent", "idea-composer-plugin")
          c
        case c => c
      }

      val in = Source.fromInputStream(connection.getInputStream, Codec.UTF8.charSet.name())
      try {
        in.getLines().mkString
      } finally {
        in.close()
      }
    }
  }
}
