package org.psliwa.idea.composerJson.composer.model.repository

import org.psliwa.idea.composerJson.composer.model.PackageName

private class ComposedRepository[Package](repositories: List[Repository[Package]]) extends Repository[Package] {
  override def getPackages: Seq[Package] = {
    repositories.flatMap(_.getPackages)
  }

  override def getPackageVersions(packageName: PackageName): Seq[String] = {
    repositories
      .flatMap(_.getPackageVersions(packageName))
  }

  override def map[NewPackage](f: Package => NewPackage): Repository[NewPackage] =
    new ComposedRepository(repositories.map(_ map f))
}
