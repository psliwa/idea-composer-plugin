package org.psliwa.idea.composerJson.composer.repository

import org.psliwa.idea.composerJson.composer.repository.DefaultRepositoryProvider.{DefaultRepositoryFactory, RepositoryFactory}
import org.psliwa.idea.composerJson.composer.parsers.{JsonParsers, RepositoryPackages}
import org.psliwa.idea.composerJson.util.IO
import scala.collection.mutable

class DefaultRepositoryProvider[Package](repositoryFactory: RepositoryFactory[Package], defaultRepository: Repository[Package]) extends RepositoryProvider[Package] {

  //simple to use constructor with default RepositoryFactory and default configuration
  def this(packagistRepository: Repository[Package], mapPackage: String => Package) = {
    this(
      new DefaultRepositoryFactory(
        DefaultRepositoryProvider.repositoryFromUrl(IO.loadUrl, JsonParsers.parsePackages),
        packagistRepository,
        mapPackage
      ),
      packagistRepository
    )
  }

  private val repositories = mutable.Map[String, Repository[Package]]()
  private val infos = mutable.Map[String, RepositoryInfo]()

  override def repositoryFor(file: String): Repository[Package] = {
    repositories.getOrElse(file, getRepositoryFromFactory(file))
  }
  override def updateRepository(file: String, info: RepositoryInfo): Unit = {
    infos(file) = info
    repositories -= file
  }

  private def getRepositoryFromFactory(file: String): Repository[Package] = {
    val repository = infos
      .get(file)
      .map(repositoryFactory.repositoryFor)
      .getOrElse(defaultRepository)

    repositories(file) = repository

    repository
  }
}

object DefaultRepositoryProvider {
  private[repository] trait RepositoryFactory[Package] {
    def repositoryFor(repositoryInfo: RepositoryInfo): Repository[Package]
  }

  private[repository] class DefaultRepositoryFactory[Package](
    repositoryFromUrl: String => Repository[String],
    packagistRepository: Repository[Package],
    mapPackage: String => Package
  ) extends RepositoryFactory[Package] {
    override def repositoryFor(repositoryInfo: RepositoryInfo): Repository[Package] = {
      new ComposedRepository(
        repositoryInfo.urls.map(repositoryFromUrl).map(_.map(mapPackage)) ++ List(packagistRepository).filter(_ => repositoryInfo.packagist)
      )
    }
  }

  private[repository] def repositoryFromUrl(
    loadUrl: String => Either[String, String],
    parsePackages: String => Either[String, RepositoryPackages]
  )(url: String): Repository[String] = {

    //TODO: par?

    def loadPackages(url: String): Option[RepositoryPackages] = {
      for {
        data <- loadUrl(url).right.toOption
        packages <- parsePackages(data).right.toOption
      } yield packages
    }

    def loadPackagesFromUrls(urls: Seq[String]): Map[String,Seq[String]] = {
      urls
        .flatMap(url => loadPackages(url).toList)
        .flatMap(pkgs => Seq(pkgs, new RepositoryPackages(loadPackagesFromUrls(pkgs.includes), Nil)))
        .foldLeft(Map[String,Seq[String]]())((map, pkgs) => map ++ pkgs.packages)
    }

    val maybeRepository = loadPackages(url)
      .map(pkgs => pkgs.packages ++ loadPackagesFromUrls(pkgs.includes))
      .map(pkgs => new InMemoryRepository(pkgs.keys.toList, pkgs))

    maybeRepository.getOrElse(EmptyRepository)
  }
}
