package org.psliwa.idea.composerJson.fixtures

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.{VfsUtil, VirtualFile}
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.psliwa.idea.composerJson
import org.psliwa.idea.composerJson.composer._

object ComposerFixtures {
  def writeAction[A](f: () => A): A = {
    ApplicationManager.getApplication.runWriteAction(new Computable[A] {
      override def compute = f()
    })
  }

  def saveText(file: VirtualFile, text: String) = {
    writeAction(() => VfsUtil.saveText(file, text))
  }

  private def makePackagesJson(pkgs: Iterable[ComposerPackageWithReplaces]): String = {
    def makeReplacesJson(pkg: ComposerPackageWithReplaces): String = {
      if(pkg.replaces.isEmpty) {
        ""
      } else {
        def x(pkg: String): String = s""""$pkg":"""""
        s"""
          |,"replace": {
          |${pkg.replaces.map(x).mkString(",")}
          |}
          |""".stripMargin
      }
    }

    pkgs.map( pkg =>
      s"""{
          |  "name": "${pkg.pkg.name}",
          |  ${pkg.pkg.homepage.map(homepage => s""""homepage":"$homepage",""").getOrElse("")}
          |  "version": "${pkg.pkg.version}"
          |  ${makeReplacesJson(pkg)}
          |}
        """.stripMargin
    ).mkString(",\n")
  }

  def createComposerLock(fixture: CodeInsightTestFixture, packages: List[ComposerPackageWithReplaces], dir: String = "."): VirtualFile = {

    val (devPackages, prodPackages) = packages.partition(_.pkg.isDev)

    val devPackagesJson = makePackagesJson(devPackages)
    val prodPackagesJson = makePackagesJson(prodPackages)

    val file = writeAction(() => fixture.getTempDirFixture.findOrCreateDir(dir).createChildData(this, composerJson.ComposerLock))
    saveText(file, s"""
        |{
        |  "packages": [ $prodPackagesJson ],
        |  "packages-dev": [ $devPackagesJson ]
        |}
      """.stripMargin
    )

    file
  }

  def createComposerJson(fixture: CodeInsightTestFixture, dir: String = ".") = {
    val file = writeAction(() => fixture.getTempDirFixture.findOrCreateDir(dir).createChildData(this, composerJson.ComposerJson))
    saveText(file, "{}")

    file
  }

  case class ComposerPackageWithReplaces(pkg: PackageDescriptor, replaces: Set[String] = Set.empty)
}
