package org.psliwa.idea.composerJson.util.parsers

case class Location(wholeInput: String, offset: Int = 0) {
  lazy val input = wholeInput.substring(offset)

  def advancedBy(n: Int): Location = copy(offset = offset + n)
}
