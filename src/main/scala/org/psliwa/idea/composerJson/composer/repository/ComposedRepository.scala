package org.psliwa.idea.composerJson.composer.repository

private class ComposedRepository[Package](repositories: List[Repository[Package]]) extends Repository[Package]{
  override def getPackages: Seq[Package] = {
    repositories.flatMap(_.getPackages)
  }

  override def getPackageVersions(pkg: String): Seq[String] = {
    repositories
      .flatMap(_.getPackageVersions(pkg))
  }

  override def map[NewPackage](f: (Package) => NewPackage): Repository[NewPackage] = new ComposedRepository(repositories.map(_ map f))
}
