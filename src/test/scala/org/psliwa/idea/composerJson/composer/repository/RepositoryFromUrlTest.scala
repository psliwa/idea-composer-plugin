package org.psliwa.idea.composerJson.composer.repository

import org.junit.Assert._
import org.junit.Test
import org.psliwa.idea.composerJson.composer.parsers.RepositoryPackages

class RepositoryFromUrlTest {

  @Test
  def givenValidUrl_givenValidJsonData_shouldCreateValidRepository() = {

    //given

    val url = "some-url"
    val content = "content"
    val repositoryPackages = new RepositoryPackages(Map("package" -> List("1.0.0")), List())

    val load = loadUrl(Map(url -> content)) _
    val parse = parsePackages(Map(content -> repositoryPackages)) _

    //when

    val repository = DefaultRepositoryProvider.repositoryFromUrl(load, parse)(url)

    //then

    assertTrue(repository.getPackages.contains("package"))
    assertTrue(repository.getPackageVersions("package").contains("1.0.0"))
  }

  @Test
  def givenInvalidUrl_shouldCreateEmptyRepository() = {
    //given

    val load = loadUrl(Map()) _
    val parse = parsePackages(Map()) _

    //when

    val repository = DefaultRepositoryProvider.repositoryFromUrl(load, parse)("some-url")

    //then

    assertTrue(repository.getPackages.isEmpty)
  }

  @Test
  def givenIncludesInContents_includesShouldAlsoBeLoaded() = {

    //given

    val url = "some-url"
    val includeUrl = "some-include-url"
    val content = "some-content"
    val includeContent = "some-include-content"

    val repositoryPackages = new RepositoryPackages(Map("package" -> List("1.0.0")), List(includeUrl))
    val includeRepositoryPackages = new RepositoryPackages(Map("package2" -> List("2.0.0")), List())

    val load = loadUrl(Map(url -> content, includeUrl -> includeContent)) _
    val parse = parsePackages(Map(content -> repositoryPackages, includeContent -> includeRepositoryPackages)) _

    //when

    val repository = DefaultRepositoryProvider.repositoryFromUrl(load, parse)(url)

    //then

    assertTrue(repository.getPackages.contains("package"))
    assertTrue(repository.getPackages.contains("package2"))
  }

  @Test
  def givenNestedIncludesInContents_includesShouldAlsoBeLoaded() = {
    //given

    val url = "some-url"
    val include1Url = "some-include1-url"
    val include2Url = "some-include2-url"

    val content = "some-content"
    val include1Content = "some-include1-content"
    val include2Content = "some-include2-content"

    val repositoryPackages = new RepositoryPackages(Map(), List(include1Url))
    val include1RepositoryPackages = new RepositoryPackages(Map(), List(include2Url))
    val include2RepositoryPackages = new RepositoryPackages(Map("package" -> Seq("1.0.0")), List())

    val load = loadUrl(Map(url -> content, include1Url -> include1Content, include2Url -> include2Content)) _
    val parse = parsePackages(Map(content -> repositoryPackages, include1Content -> include1RepositoryPackages, include2Content -> include2RepositoryPackages)) _


    //when

    val repository = DefaultRepositoryProvider.repositoryFromUrl(load, parse)(url)

    //then

    assertTrue(repository.getPackages.contains("package"))
  }

  private def loadUrl(contents: Map[String, String])(url: String): Either[String,String] = {
    contents.get(url).toRight("")
  }

  private def parsePackages(results: Map[String, RepositoryPackages])(data: String): Either[String,RepositoryPackages] = {
    results.get(data).toRight("")
  }
}
