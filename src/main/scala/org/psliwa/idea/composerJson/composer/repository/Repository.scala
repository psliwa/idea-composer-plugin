package org.psliwa.idea.composerJson.composer.repository

trait Repository[+Package] {
  def getPackages: Seq[Package]
  def getPackageVersions(pkg: String): Seq[String]
  def map[NewPackage](f: Package => NewPackage): Repository[NewPackage]
}

object EmptyRepository extends Repository[Nothing] {
  override def getPackages: Seq[Nothing] = Nil
  override def getPackageVersions(pkg: String) = Nil
  override def map[NewPackage](f: (Nothing) => NewPackage): Repository[NewPackage] = this
}