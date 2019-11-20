package org.psliwa.idea.composerJson.composer.model.repository

import org.psliwa.idea.composerJson.composer.model.PackageName

private class CallbackRepository[Package](packages: => Seq[Package], versions: PackageName => Seq[String])
    extends Repository[Package] {
  override def getPackages: Seq[Package] = packages
  override def getPackageVersions(packageName: PackageName): Seq[String] = versions(packageName)
  override def map[NewPackage](f: Package => NewPackage): Repository[NewPackage] =
    new CallbackRepository(packages.map(f), versions)
}
