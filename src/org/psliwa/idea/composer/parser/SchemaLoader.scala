package org.psliwa.idea.composer.parser

import scala.io.Source

object SchemaLoader {
  def load(path: String = "../composer-schema.json"): Option[Schema] = {
    Option(getClass.getResource(path))
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