package org.psliwa.idea.composerJson.composer

trait Repository[Package] {
  def getPackages: Seq[Package]
  def getPackageVersions(pkg: String): Seq[String]
}
