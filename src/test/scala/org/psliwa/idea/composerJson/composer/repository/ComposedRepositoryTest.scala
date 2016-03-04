package org.psliwa.idea.composerJson.composer.repository

import org.psliwa.idea.composerJson.BasePropSpec
import org.scalacheck.Prop
import org.scalacheck.Prop.{AnyOperators, forAll}

class ComposedRepositoryTest extends BasePropSpec {
  import RepositoryGenerators._

  property("contains packages from all repositories") {
    forAll(repositoryWithPkgsGen) { case(repository: Repository[String], packages: Seq[String]) =>
      repository.getPackages ?= packages
    }
  }

  property("contains versions from all repositories") {
    forAll(repositoryWithVersionsGen) { case(repository: Repository[String], pkgsVersions: Map[String, Seq[String]]) =>
      Prop.all(pkgsVersions.map { case(pkg, versions) => (repository.getPackageVersions(pkg).toList ?= versions.toList) :| s"$versions" }.toSeq: _*)
    }
  }

}
