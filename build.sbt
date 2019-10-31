import java.io.File

import sbt.Keys.{`package` => pack}
import sbt.project

import scala.language.postfixOps

addCommandAlias("pluginRun", "runner/run")
addCommandAlias("pluginCompress", "compressor/package")
addCommandAlias("pluginPack", "packager/package")
addCommandAlias("pluginProguard", "proguard/package")

val phpPluginUrl = settingKey[String]("Php plugin url")

onLoad in Global := ((s: State) => { "updateIdea" :: s}) compose (onLoad in Global).value

ideaBuild in ThisBuild := sys.props.getOrElse("IDEA_VERSION", "2019.2.3")
phpPluginUrl in ThisBuild := sys.props.getOrElse("PHP_PLUGIN_URL", "https://plugins.jetbrains.com/files/6610/68963/php-192.6817.12.zip")
ideaEdition in ThisBuild := IdeaEdition.Ultimate
scalaVersion in ThisBuild := Versions.scala

lazy val release = TaskKey[Unit]("release")
release in ThisBuild := {
  clean.value
  compile.in(root, Compile).dependsOn(clean).value
  pack.in(packager).dependsOn(compile.in(root, Compile)).value
  pack.in(compressor).dependsOn(pack.in(packager)).value
  pack.in(proguard).dependsOn(pack.in(compressor)).value
}

lazy val root = (project in file("."))
  .enablePlugins(SbtIdeaPlugin)
  .settings(
    name := "composer-json-plugin",
    autoScalaLibrary := false,
    scalaVersion := Versions.scala,
    javacOptions in Global ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions in Global += "-target:jvm-1.8",

    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-library" % Versions.scala,
      "org.scala-lang" % "scala-compiler" % Versions.scala,
      "org.scala-lang.modules" %% "scala-parser-combinators" % Versions.scalaParsers,
      "io.spray" %%  "spray-json" % Versions.sprayJson,
      "org.scalaz" %% "scalaz-core" % Versions.scalaz,
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    ),
    ideaInternalPlugins ++= Seq(
      "java-i18n",
      "properties",
      "CSS",
      "java"
    ),
    ideaExternalPlugins ++= Seq(
      IdeaPlugin.Zip("php", url(phpPluginUrl.value))
    ),

    unmanagedJars in Test += file(System.getProperty("java.home")).getParentFile / "lib" / "tools.jar",
    fork in Test := false,
    parallelExecution := false,
    javaOptions in Test := {
      def ivyHomeDir: File =
        Option(System.getProperty("sbt.ivy.home")).fold(Path.userHome / ".ivy2")(file)
      Seq(
        "-Xms128m",
        "-Xmx4096m",
        "-XX:MaxPermSize=350m",
        "-ea",
        s"-Didea.system.path=${ideaBaseDirectory.value.getAbsoluteFile}/test-system",
        s"-Didea.config.path=${ideaBaseDirectory.value.getAbsoluteFile}/test-config",
        s"-Dsbt.ivy.home=$ivyHomeDir",
        s"-Dplugin.path=${packagedPluginDir.value}",
        s"-Didea.home.path=${ideaBaseDirectory.value.getAbsolutePath}"
      )
    },
    envVars in Test += "NO_FS_ROOTS_ACCESS_CHECK" -> "yes"
  )

def getBaseDir(baseDir: File) = baseDir.getParentFile.getParentFile

val getBaseDirPath = getBaseDir _ andThen (_.getAbsolutePath)

lazy val runner = (project in file("subprojects/idea-runner"))
  .dependsOn(root % Provided)
  .settings(
    autoScalaLibrary := false,
    unmanagedJars in Compile := ideaMainJars.in(root).value,
    unmanagedJars in Compile += file(System.getProperty("java.home")).getParentFile / "lib" / "tools.jar",
    fork in run := true,
    mainClass in (Compile, run) := Some("com.intellij.idea.Main"),
    javaOptions in run ++= Seq(
      "-Xmx1024m",
      "-XX:ReservedCodeCacheSize=256m",
      "-XX:MaxPermSize=250m",
      "-XX:+HeapDumpOnOutOfMemoryError",
      "-ea",
      "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005",
      "-Didea.is.internal=true",
      "-Didea.debug.mode=true",
      "-Didea.case.sensitive.fs=true",
      "-Dapple.laf.useScreenMenuBar=true",
      s"-Dplugin.path=${packagedPluginDir.value}",
      "-Didea.ProcessCanceledException=disabled"
    ),
    run in Compile <<= run in Compile dependsOn pack.in(packager)
  )

lazy val packagedPluginDir = settingKey[File]("Path to packaged, but not yet compressed plugin")

packagedPluginDir in ThisBuild := baseDirectory.in(ThisBuild).value / "target" / name.in(root).value

lazy val packager: Project = (project in file("subprojects/packager"))
  .settings(
    artifactPath := packagedPluginDir.value,
    dependencyClasspath <<= {
      dependencyClasspath in (root, Compile)
    },
    mappings := {
      val pluginMapping = pack.in(root, Compile).value -> "lib/composer-json-plugin.jar"

      def compilerLibrary(file: Attributed[File]) = {
        file.get(moduleID.key).exists(_.name.contains("compiler"))
      }

      val dependenciesMapping = dependencyClasspath.in(root, Compile).value
        .filter(_.get(moduleID.key).isDefined)
        .filterNot(compilerLibrary)
        .map { file =>
          val moduleId = file.get(moduleID.key).get
          file.data -> s"lib/${moduleId.name}.jar"
        }.toList

      pluginMapping :: dependenciesMapping
    },
    pack := {
      val destination = artifactPath.value
      IO.delete(destination)
      val (dirs, files) = mappings.value.partition(_._1.isDirectory)
      dirs  foreach { case (from, to) => IO.copyDirectory(from, destination / to, overwrite = true) }
      files foreach { case (from, to) => IO.copyFile(from, destination / to)}

      artifactPath.value
    }
  )

lazy val compressor: Project = (project in file("subprojects/compressor"))
  .settings(
    artifactPath := getBaseDir(baseDirectory.value) / "target" / "composer-json-plugin.zip",
    description := "Prepare plugin zip file",
    pack := {
      val source = pack.in(packager).value
      IO.zip((source ***) pair (relativeTo(source.getParentFile), false), artifactPath.value)
      artifactPath.value
    }
  )

lazy val proguard: Project = (project in file("subprojects/proguard"))
  .settings(
    artifactPath := getBaseDir(baseDirectory.value) / "target" / "composer-json-plugin-proguard.zip",
    pack := {
      val proguardUrl = "https://github.com/psliwa/proguard-fixd/raw/master/proguard6.2.0.jar"
      val proguardDest: File = getBaseDir(baseDirectory.value) / "proguard.jar"

      if(!proguardDest.exists()) {
        IO.download(new URL(proguardUrl), proguardDest)
      }

      def escape(s: String) = if(File.pathSeparatorChar == '\\') "\""+s+"\"" else s

      val javaRt = file(System.getProperty("java.home")) / "lib" / "rt.jar"

      val libraryJars = (javaRt :: ideaFullJars.in(root).value.map(_.data).toList)
        .map(_.getAbsolutePath)
        .map(escape)
        .mkString(sys.props.getOrElse("path.separator", ":"))

      val cmd = Seq(
        "java", "-jar", escape(proguardDest.getAbsolutePath), "@"+escape(getBaseDirPath(baseDirectory.value)+"/proguard.pro"), "-injars", escape(artifactPath.in(compressor, Compile).value.getAbsolutePath), "-outjars", escape(artifactPath.value.getAbsolutePath),
        "-libraryjars", libraryJars
      )

      val result = cmd !

      if(result != 0) {
        sys.error(s"Proguard exit status: $result")
      }

      artifactPath.value
    }
  )