package org.psliwa.idea.composerJson.composer.repository

case class FakeRepository(packages: Seq[String], versions: Map[String, List[String]] = Map()) extends Repository[String] {
  override def getPackages: Seq[String] = packages
  override def getPackageVersions(pkg: String): Seq[String] = versions.getOrElse(pkg, List())
}