package org.psliwa.idea.composerJson.composer.model.repository

import org.scalacheck.Gen
import scalaz._
import Scalaz._
import org.psliwa.idea.composerJson.composer.model.PackageName

object RepositoryGenerators {

  private implicit val _ = seqMonoid[String]

  private def pkgGen: Gen[PackageName] = Gen.listOfN(5, Gen.alphaLowerChar).map(_.mkString("")).map(PackageName)
  private def versionGen: Gen[String] = for {
    size <- Gen.choose(1, 2)
    chars <- Gen.listOfN(size, Gen.alphaChar)
  } yield chars.mkString("")
  private def versionsGen: Gen[List[String]] = for {
    size <- Gen.choose(0, 50)
    versions <- Gen.listOfN(size, versionGen)
  } yield versions
  def repositoryWithPackageNamesGen: Gen[(Repository[String], Seq[PackageName])] = for {
    n <- Gen.choose(0, 20)
    packagesNames <- Gen.listOfN(n, Gen.listOf(pkgGen))
    repos = packagesNames.map(packageNames => InMemoryRepository(packageNames.map(_.presentation)))
  } yield (new ComposedRepository(repos), packagesNames.flatten)

  private def pkgsVersionsGen(packageNames: Seq[PackageName]): Gen[Map[PackageName, Seq[String]]] = for {
    packagesAndVersions <- Gen.sequence[Seq[(PackageName, Seq[String])], (PackageName, Seq[String])](packageNames.map(packageName => versionsGen.map(packageName -> _)))
  } yield packagesAndVersions.toMap

  def repositoryWithVersionsGen: Gen[(Repository[String], Map[PackageName, Seq[String]])] = for {
    pkgsCount <- Gen.choose(2, 4)
    packageNames <- Gen.listOfN(pkgsCount, pkgGen)
    reposCount <- Gen.choose(1, 4)
    versions <- Gen.listOfN(reposCount, pkgsVersionsGen(packageNames))
    repos = versions.map(InMemoryRepository(packageNames.map(_.presentation), _))
  } yield (new ComposedRepository(repos), versions.reduce[Map[PackageName, Seq[String]]](_ |+| _))

  private def seqMonoid[A]: Monoid[Seq[A]] = new Monoid[Seq[A]] {
    override def zero: Seq[A] = Seq.empty
    override def append(f1: Seq[A], f2: => Seq[A]): Seq[A] = f1 ++ f2
  }

}
