package org.psliwa.idea.composerJson.composer.repository

private class CallbackRepository[Package](packages: => Seq[Package], versions: String => Seq[String]) extends Repository[Package] {
  override def getPackages: Seq[Package] = packages
  override def getPackageVersions(pkg: String): Seq[String] = versions(pkg)
  override def map[NewPackage](f: (Package) => NewPackage): Repository[NewPackage] = new CallbackRepository(packages.map(f), versions)
}
