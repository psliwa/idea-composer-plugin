package org.psliwa.idea.composerJson.composer.repository

import org.psliwa.idea.composerJson.composer.model.PackageName
import org.psliwa.idea.composerJson.composer.parsers.JsonParsers.{parsePackageNames, parseVersions}
import org.psliwa.idea.composerJson.util.IO

import scala.util.Try

object Packagist {

  val defaultUrl: String = "https://packagist.org/"

  val privatePackagistUrl: String = "https://repo.packagist.com"

  def loadPackages(url: String): Try[Seq[String]] = loadPackagesFromPackagist(url).flatMap(parsePackageNames)
  def loadVersions(url: String)(packageName: PackageName): Try[Seq[String]] =
    loadUri(url)(s"packages/${packageName.presentation}.json").flatMap(parseVersions)

  private[repository] def loadPackagesFromPackagist(url: String): Try[String] = loadUri(url)("packages/list.json")
  private[repository] def loadUri(url: String)(uri: String): Try[String] = {
    val fixedUrl = if (!url.lastOption.contains('/')) url + "/" else url
    IO.loadUrl(fixedUrl + uri)
  }
}
