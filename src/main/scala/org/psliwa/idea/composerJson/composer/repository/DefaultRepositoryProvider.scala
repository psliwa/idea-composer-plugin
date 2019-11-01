package org.psliwa.idea.composerJson.composer.repository

import org.psliwa.idea.composerJson.composer.repository.DefaultRepositoryProvider.{DefaultRepositoryFactory, RepositoryFactory}
import org.psliwa.idea.composerJson.composer.parsers.{JsonParsers, RepositoryPackages}
import org.psliwa.idea.composerJson.util.IO
import scala.collection.mutable
import scala.util.Try

class DefaultRepositoryProvider[Package](repositoryFactory: RepositoryFactory[Package], defaultRepository: Repository[Package]) extends RepositoryProvider[Package] {

  //simple to use constructor with default RepositoryFactory and default configuration
  def this(packagistRepository: Repository[Package], mapPackage: String => Package) = {
    this(
      new DefaultRepositoryFactory(
        DefaultRepositoryProvider.repositoryFromUrl,
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

  override def updateRepository(file: String, info: RepositoryInfo) = {
    val changed = !infos.get(file).contains(info)

    if(changed) {
      infos(file) = info
      repositories -= file
    }

    changed
  }

  private def getRepositoryFromFactory(file: String): Repository[Package] = {
    val repository = infos
      .get(file)
      .map(repositoryFactory.repositoryFor)
      .getOrElse(defaultRepository)

    repositories(file) = repository

    repository
  }

  override def hasDefaultRepository(file: String): Boolean = {
    infos.get(file).forall(info => info.packagist && info.urls.isEmpty || info.urls.nonEmpty && info.urls.forall(_.startsWith(Packagist.defaultUrl)))
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
      new SkipBuiltInPackagesVersionRepository(
        new ComposedRepository(
          repositoryInfo.urls.map(repositoryFromUrl).map(_.map(mapPackage)) ++
            List(packagistRepository).filter(_ => repositoryInfo.packagist) ++
            repositoryInfo.repository.map(_.map(mapPackage)).toList
        )
      )
    }
  }

  private def repositoryFromUrl(url: String): Repository[String] = {
    packagistRepositoryFromUrl(url) orElse satisRepositoryFromUrl(IO.loadUrl, JsonParsers.parsePackages)(url) getOrElse EmptyRepository
  }

  private def rootUrlOf(url: String) = "http(s?)://[^/]+".r.findFirstIn(url).getOrElse(url)

  private[repository] def satisRepositoryFromUrl(
    loadUrl: String => Try[String],
    parsePackages: String => Try[RepositoryPackages]
  )(url: String): Option[Repository[String]] = {

    val rootUrl = rootUrlOf(url)
    def buildUrl(uri: String): String = rootUrl+"/"+uri

    def loadPackages(url: String): Option[RepositoryPackages] = {
      for {
        data <- loadUrl(url).toOption
        packages <- parsePackages(data).toOption
      } yield packages
    }

    def loadPackagesFromUrls(urls: Seq[String]): Map[String,Seq[String]] = {
      urls.par
        .flatMap(loadPackages(_).toList)
        .flatMap(pkgs => Seq(pkgs, new RepositoryPackages(loadPackagesFromUrls(pkgs.includes.map(buildUrl)), Nil)))
        .foldLeft(Map[String,Seq[String]]())((map, pkgs) => map ++ pkgs.packages)
    }

    loadPackages(url)
      .map(pkgs => pkgs.packages ++ loadPackagesFromUrls(pkgs.includes.map(buildUrl)))
      .map(pkgs => new InMemoryRepository(pkgs.keys.toList, pkgs))
  }

  private def packagistRepositoryFromUrl(url: String): Option[Repository[String]] = {
    import org.psliwa.idea.composerJson.util.Funcs._
    val packages: Try[Seq[String]] = Packagist.loadPackages(url)
    packages.toOption
      .map(packages => new CallbackRepository[String](packages, memorize(30)(Packagist.loadVersions(url)(_).getOrElse(List.empty))))
  }
}
