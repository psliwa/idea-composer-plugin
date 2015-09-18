package org.psliwa.idea.composerJson.composer.repository

import org.psliwa.idea.composerJson.composer.parsers.JsonParsers._
import org.psliwa.idea.composerJson.util.IO

import scala.util.Try

object Packagist {

  private val PackagistUrl = "https://packagist.org/"

  def loadPackages(): Try[Seq[String]] = loadPackagesFromPackagist().flatMap(parsePackageNames)
  def loadVersions(pkg: String): Try[Seq[String]] = loadUri("packages/"+pkg+".json").flatMap(parseVersions)

  private[repository] def loadPackagesFromPackagist(): Try[String] = loadUri("packages/list.json")
  private[repository] def loadUri(uri: String): Try[String] = {
    IO.loadUrl(PackagistUrl+uri)
  }
}
