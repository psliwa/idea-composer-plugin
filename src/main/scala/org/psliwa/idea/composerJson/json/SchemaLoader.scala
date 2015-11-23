package org.psliwa.idea.composerJson.json

import scala.io.Source
import scala.util.Try

object SchemaLoader {
  def load(path: String): Option[Schema] = {
    Option(this.getClass.getResource(path))
      .map(Source.fromURL)
      .flatMap(consumeSource)
      .flatMap(Schema.parse)
  }

  private def consumeSource(s: Source): Option[String] = {
    Try {
      try {
        s.mkString
      } finally {
        s.close()
      }
    }.toOption
  }
}
