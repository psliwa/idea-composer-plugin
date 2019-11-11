package org.psliwa.idea.composerJson.composer.model.repository

import org.psliwa.idea.composerJson.composer.model.PackageName

private class SkipBuiltInPackagesVersionRepository[Package](underlyingRepository: Repository[Package]) extends Repository[Package] {
  override def getPackages: Seq[Package] = underlyingRepository.getPackages

  override def getPackageVersions(packageName: PackageName): Seq[String] = {
    packageName.vendor match {
      case Some(_) => underlyingRepository.getPackageVersions(packageName)
      case None => Seq.empty
    }
  }

  override def map[NewPackage](f: Package => NewPackage): Repository[NewPackage] =
    new SkipBuiltInPackagesVersionRepository[NewPackage](underlyingRepository.map(f))
}
