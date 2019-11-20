import sbt.Keys.{`package` => pack}

import scala.sys.process._
import java.io.File

import org.jetbrains.sbtidea.Keys._

addCommandAlias("createRunConfiguration", "; idea-runner/createIDEARunConfiguration ; idea-runner/createIDEAArtifactXml")
addCommandAlias("release", "proguard/package")

val phpPluginVersion = settingKey[String]("Php plugin version")

intellijBuild in ThisBuild := sys.props.getOrElse("IDEA_VERSION", Versions.idea)
phpPluginVersion in ThisBuild := sys.props.getOrElse("PHP_PLUGIN_VERSION", Versions.phpPlugin)
intellijPlatform in ThisBuild := IntelliJPlatform.IdeaUltimate
scalaVersion in ThisBuild := Versions.scala
intellijDownloadDirectory in ThisBuild := file("./idea")

lazy val ideaComposerPlugin = (project in file("."))
  .enablePlugins(SbtIdeaPlugin)
  .settings(
    autoScalaLibrary := false,
    scalaVersion := Versions.scala,
    javacOptions in Global ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions in Global += "-target:jvm-1.8",
    ideBasePackages := Seq("org.jetbrains.plugins.hocon"),
    ideOutputDirectory in Compile := Some(file("out/production")),
    ideOutputDirectory in Test := Some(file("out/test")),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-library" % Versions.scala,
      "org.scala-lang" % "scala-compiler" % Versions.scala,
      "org.scala-lang.modules" %% "scala-parser-combinators" % Versions.scalaParsers,
      "io.spray" %%  "spray-json" % Versions.sprayJson,
      "org.scalaz" %% "scalaz-core" % Versions.scalaz,
      "com.novocode" % "junit-interface" % Versions.junitInterface % "test",
      "org.scalacheck" %% "scalacheck" % Versions.scalacheck % "test",
      "org.scalatest" %% "scalatest" % Versions.scalatest % "test"
    ),
    intellijInternalPlugins ++= Seq(
      "java-i18n",
      "properties",
      "CSS",
      "java"
    ),
    intellijExternalPlugins ++= Seq(
      IntellijPlugin.Id("com.jetbrains.php", Some(phpPluginVersion.value), None)
    ),
    intellijVMOptions := intellijVMOptions.value.copy(xmx = 4096, xms = 256)
  )

lazy val benchmarks = (project in file("benchmarks"))
  .enablePlugins(JmhPlugin)
  .dependsOn(ideaComposerPlugin)

lazy val ideaRunner = createRunnerProject(ideaComposerPlugin, "idea-runner")

def getBaseDir(baseDir: File) = baseDir.getParentFile.getParentFile

val getBaseDirPath = getBaseDir _ andThen (_.getAbsolutePath)

lazy val proguard: Project = (project in file("target/proguard"))
  .dependsOn(ideaComposerPlugin)
  .settings(
    artifactPath := getBaseDir(baseDirectory.value) / "target" / "composer-json-plugin-proguard.zip",
    pack := {
      val proguardUrl = "https://github.com/psliwa/proguard-fixd/raw/master/proguard6.2.0.jar"
      val proguardDest: File = getBaseDir(baseDirectory.value) / "proguard.jar"

     def download(url: URL, to: File): Unit =
       sbt.io.Using.urlInputStream(url) { inputStream =>
         IO.transfer(inputStream, to)
       }

      if(!proguardDest.exists()) {
        download(new URL(proguardUrl), proguardDest)
      }

      def escape(s: String) = if(File.pathSeparatorChar == '\\') "\""+s+"\"" else s

      val javaRt = file(System.getProperty("java.home")) / "lib" / "rt.jar"

      val libraryJars = (javaRt :: intellijFullJars.in(ideaComposerPlugin).value.map(_.data).toList)
        .map(_.getAbsolutePath)
        .map(escape)
        .mkString(sys.props.getOrElse("path.separator", ":"))


      val cmd = Seq(
        "java", "-jar", escape(proguardDest.getAbsolutePath), "@"+escape(getBaseDirPath(baseDirectory.value)+"/proguard.pro"), "-injars", escape(packageArtifactZip.in(ideaComposerPlugin).value.getAbsolutePath), "-outjars", escape(artifactPath.value.getAbsolutePath),
        "-libraryjars", libraryJars
      )

      val result = cmd !

      if(result != 0) {
        sys.error(s"Proguard exit status: $result")
      }

      artifactPath.value
    }
  ).enablePlugins(SbtIdeaPlugin)
