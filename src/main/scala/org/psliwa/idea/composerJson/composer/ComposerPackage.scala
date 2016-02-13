package org.psliwa.idea.composerJson.composer

case class ComposerPackage(name: String, version: String, isDev: Boolean = false)

object ComposerPackage {
  def `vendor/package`(s: String): Option[(String, String)] = {
    s.split("/") match {
      case Array(vendor, pkg) => Some((vendor, pkg))
      case _ => None
    }
  }

  def packagistUrl(pkg: String) = s"https://packagist.org/packages/$pkg"
}
