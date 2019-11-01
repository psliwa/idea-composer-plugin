package org.psliwa.idea.composerJson.composer.repository

trait Repository[+Package] {
  def getPackages: Seq[Package]
  def getPackageVersions(pkg: String): Seq[String]
  def map[NewPackage](f: Package => NewPackage): Repository[NewPackage]
}

object Repository {
  def inMemory[Package](packages: Seq[Package], versions: Map[String, Seq[String]] = Map()): Repository[Package] = {
    InMemoryRepository(packages, versions)
  }

  def callback[Package](packages: => Seq[Package], versions: String => Seq[String]): Repository[Package] = {
    new SkipBuiltInPackagesVersionRepository(new CallbackRepository[Package](packages, versions))
  }
}

private object EmptyRepository extends Repository[Nothing] {
  override def getPackages: Seq[Nothing] = Nil
  override def getPackageVersions(pkg: String): Seq[String] = Nil
  override def map[NewPackage](f: (Nothing) => NewPackage): Repository[NewPackage] = this
}