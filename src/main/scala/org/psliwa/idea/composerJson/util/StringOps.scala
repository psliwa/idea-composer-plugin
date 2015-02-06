package org.psliwa.idea.composerJson.util

class StringOps(s: String) {
  def stripQuotes = s.stripPrefix("\"").stripSuffix("\"")
}