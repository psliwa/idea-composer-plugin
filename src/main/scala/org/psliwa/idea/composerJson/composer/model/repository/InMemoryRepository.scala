package org.psliwa.idea.composerJson.composer.model.repository

import org.psliwa.idea.composerJson.composer.model.PackageName

private case class InMemoryRepository[Package](packages: Seq[Package], versions: Map[PackageName, Seq[String]] = Map()) extends Repository[Package] {
  override def getPackages: Seq[Package] = packages
  override def getPackageVersions(packageName: PackageName): Seq[String] = versions.getOrElse(packageName, Nil)
  override def map[NewPackage](f: (Package) => NewPackage): Repository[NewPackage] = new InMemoryRepository(packages.map(f), versions)
}