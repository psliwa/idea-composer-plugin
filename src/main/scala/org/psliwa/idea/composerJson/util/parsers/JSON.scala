package org.psliwa.idea.composerJson.util.parsers

import spray.json.{JsValue, JsonParser}

import scala.util.Try

object JSON {
  def parse(data: String): Option[JsValue] = Try { JsonParser(data) }.toOption
}
