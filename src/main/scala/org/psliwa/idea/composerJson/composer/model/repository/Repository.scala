package org.psliwa.idea.composerJson.composer.model.repository

import org.psliwa.idea.composerJson.composer.model.PackageName

trait Repository[+Package] {
  def getPackages: Seq[Package]
  def getPackageVersions(packageName: PackageName): Seq[String]
  def map[NewPackage](f: Package => NewPackage): Repository[NewPackage]
}

object Repository {
  def inMemory[Package](packages: Seq[Package], versions: Map[String, Seq[String]] = Map()): Repository[Package] = {
    InMemoryRepository(packages, versions.map { case (packageName, versions) => PackageName(packageName) -> versions })
  }

  def callback[Package](packages: => Seq[Package], versions: PackageName => Seq[String]): Repository[Package] = {
    new SkipBuiltInPackagesVersionRepository(new CallbackRepository[Package](packages, versions))
  }

  def composed[Package](repositories: List[Repository[Package]]): Repository[Package] = {
    new SkipBuiltInPackagesVersionRepository(new ComposedRepository(repositories))
  }

  def empty[Package]: Repository[Package] = EmptyRepository
}

private object EmptyRepository extends Repository[Nothing] {
  override def getPackages: Seq[Nothing] = Nil
  override def getPackageVersions(packageName: PackageName): Seq[String] = Nil
  override def map[NewPackage](f: Nothing => NewPackage): Repository[NewPackage] = this
}
