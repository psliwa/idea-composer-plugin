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

  def createComposerLock(fixture: CodeInsightTestFixture, packages: Packages, dir: String = ".") = {
    val packagesJson = packages.map{ case(name,version) =>
      s"""{
        |  "name": "$name",
        |  "version": "$version"
        |}
      """.stripMargin
    }.mkString(",\n")

    val file = writeAction(() => fixture.getTempDirFixture.findOrCreateDir(dir).createChildData(this, composerJson.ComposerLock))
    saveText(file, s"""
        |{
        |  "packages": [ $packagesJson ]
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
}
