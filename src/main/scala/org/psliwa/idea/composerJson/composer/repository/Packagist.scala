package org.psliwa.idea.composerJson.composer.repository

import org.psliwa.idea.composerJson.composer.parsers.JsonParsers._
import org.psliwa.idea.composerJson.util.IO

object Packagist {

  private val PackagistUrl = "https://packagist.org/"

  def loadPackages(): Either[Error,Seq[String]] = loadPackagesFromPackagist().right.flatMap(parsePackageNames)
  def loadVersions(pkg: String): Either[Error,Seq[String]] = loadUri("packages/"+pkg+".json").right.flatMap(parseVersions)

  private[repository] def loadPackagesFromPackagist(): Either[Error,String] = loadUri("packages/list.json")
  private[repository] def loadUri(uri: String): Either[Error,String] = {
    IO.loadUrl(PackagistUrl+uri)
  }
}
