package org.psliwa.idea.composerJson.composer.repository

private class SkipBuiltInPackagesVersionRepository[Package](underlyingRepository: Repository[Package]) extends Repository[Package] {
  override def getPackages: Seq[Package] = underlyingRepository.getPackages

  override def getPackageVersions(pkg: String): Seq[String] =
    if(pkg.contains("/")) underlyingRepository.getPackageVersions(pkg) else Seq.empty

  override def map[NewPackage](f: Package => NewPackage): Repository[NewPackage] =
    new SkipBuiltInPackagesVersionRepository[NewPackage](underlyingRepository.map(f))
}
