package org.psliwa.idea.composerJson.composer.model.repository

import org.psliwa.idea.composerJson.BasePropSpec
import org.psliwa.idea.composerJson.composer.model.PackageName
import org.scalacheck.Prop
import org.scalacheck.Prop.{forAll, AnyOperators}

class ComposedRepositoryTest extends BasePropSpec {
  import RepositoryGenerators._

  property("contains packages from all repositories") {
    forAll(repositoryWithPackageNamesGen) {
      case (repository: Repository[String], packages: Seq[PackageName]) =>
        repository.getPackages ?= packages.map(_.presentation)
    }
  }

  property("contains versions from all repositories") {
    forAll(repositoryWithVersionsGen) {
      case (repository: Repository[String], pkgsVersions: Map[PackageName, Seq[String]]) =>
        Prop.all(pkgsVersions.map {
          case (pkg, versions) => (repository.getPackageVersions(pkg).toList ?= versions.toList) :| s"$versions"
        }.toSeq: _*)
    }
  }

}
