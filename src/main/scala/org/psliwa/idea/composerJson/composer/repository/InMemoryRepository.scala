package org.psliwa.idea.composerJson.composer.repository

private case class InMemoryRepository[Package](packages: Seq[Package], versions: Map[String, Seq[String]] = Map()) extends Repository[Package] {
  override def getPackages: Seq[Package] = packages
  override def getPackageVersions(pkg: String): Seq[String] = versions.getOrElse(pkg, Nil)
  override def map[NewPackage](f: (Package) => NewPackage): Repository[NewPackage] = new InMemoryRepository(packages.map(f), versions)
}