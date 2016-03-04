package org.psliwa.idea.composerJson.composer.repository

import org.scalacheck.Prop
import org.scalacheck.Prop.{AnyOperators, forAll, forAllNoShrink}
import org.scalatest.PropSpec
import org.scalatest.prop.Checkers


class ComposedRepositoryTest extends PropSpec with Checkers {
  import RepositoryGenerators._

  property("contains packages from all repositories") {
    check(forAll(repositoryWithPkgsGen) { case(repository: Repository[String], packages: Seq[String]) =>
      repository.getPackages ?= packages
    })
  }

  property("contains versions from all repositories") {
    check(forAllNoShrink(repositoryWithVersionsGen) { case(repository: Repository[String], pkgsVersions: Map[String, Seq[String]]) =>
      Prop.all(pkgsVersions.map { case(pkg, versions) => (repository.getPackageVersions(pkg).toList ?= versions.toList) :| s"$versions" }.toSeq: _*)
    })
  }

}
