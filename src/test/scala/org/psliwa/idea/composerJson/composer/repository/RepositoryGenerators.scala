package org.psliwa.idea.composerJson.composer.repository

import org.scalacheck.Gen
import scalaz._
import Scalaz._

object RepositoryGenerators {

  private implicit val _ = seqMonoid[String]

  private def pkgGen = Gen.listOfN(5, Gen.alphaLowerChar).map(_.mkString(""))
  private def versionGen = for {
    size <- Gen.choose(1, 2)
    chars <- Gen.listOfN(size, Gen.alphaChar)
  } yield chars.mkString("")
  private def versionsGen = for {
    size <- Gen.choose(0, 50)
    versions <- Gen.listOfN(size, versionGen)
  } yield versions
  def repositoryWithPkgsGen: Gen[(Repository[String], Seq[String])] = for {
    n <- Gen.choose(0, 20)
    pkgs <- Gen.listOfN(n, Gen.listOf(pkgGen))
    repos = pkgs.map(InMemoryRepository(_))
  } yield (new ComposedRepository(repos), pkgs.flatten)

  private def pkgsVersionsGen(pkgs: Seq[String]): Gen[Map[String, Seq[String]]] = for {
    aaa <- Gen.sequence[Seq[(String, Seq[String])], (String ,Seq[String])](pkgs.map(pkg => versionsGen.map(pkg -> _)))
  } yield aaa.toMap

  def repositoryWithVersionsGen: Gen[(Repository[String], Map[String,Seq[String]])] = for {
    pkgsCount <- Gen.choose(2, 4)
    pkgs <- Gen.listOfN(pkgsCount, pkgGen)
    reposCount <- Gen.choose(1, 4)
    versions <- Gen.listOfN(reposCount, pkgsVersionsGen(pkgs))
    repos = versions.map(new InMemoryRepository(pkgs, _))
  } yield (new ComposedRepository(repos), versions.reduce[Map[String, Seq[String]]](_ |+| _))

  private def seqMonoid[A]: Monoid[Seq[A]] = new Monoid[Seq[A]] {
    override def zero: Seq[A] = Seq.empty
    override def append(f1: Seq[A], f2: => Seq[A]): Seq[A] = f1 ++ f2
  }

}
