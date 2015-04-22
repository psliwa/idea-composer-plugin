package org.psliwa.idea.composerJson.composer.repository

trait Repository[+Package] {
  def getPackages: Seq[Package]
  def getPackageVersions(pkg: String): Seq[String]
}

object EmptyRepository extends Repository[Nothing] {
  def getPackages: Seq[Nothing] = Nil
  def getPackageVersions(pkg: String) = Nil
}