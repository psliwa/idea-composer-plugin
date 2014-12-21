package org.psliwa.idea.composerJson.composer

import scala.io.Source

object SchemaLoader {
  def load(path: String): Option[Schema] = {
    Option(this.getClass.getResource(path))
      .map(Source.fromURL)
      .flatMap(consumeSource)
      .flatMap(Schema.parse)
  }

  private def consumeSource(s: Source): Option[String] = {
    try {
      try {
        Some(s.mkString)
      } finally {
        s.close()
      }
    } catch {
      case _: Throwable => None
    }
  }
}
