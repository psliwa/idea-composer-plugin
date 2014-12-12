//script that shrink (removes unused classes) plugin release zip. Thanks to that script, output file is 10x smaller

import java.io.{File, FileOutputStream, BufferedOutputStream, IOException}
import java.net.URL
import java.util.Properties

import scala.collection.JavaConverters._

import scala.io.Source
import scala.language.{postfixOps, implicitConversions}

//some types, implicit conversions, constants...

type Error = String

implicit def optionToEither[A](o: Option[A]): Either[Error,A] = o match {
  case Some(a) => Right(a)
  case _ => Left("value not found")
}

implicit def stringToEither(s: String): Either[Error,String] = Right(s)

val proguardUrl = "https://github.com/psliwa/proguard-fixd/raw/master/proguard.jar"

//write and read props - remembering provided inputs

def loadProps(): Map[String,String] = {
  val props = new Properties()

  try {
    val in = Source.fromFile("shrink.properties")
    try {
      props.load(in.bufferedReader())
    } finally {
      in.close()
    }
  } catch {
    case _: IOException => //just ignore
  }

  props.asScala.toMap
}

def writeProps(map: Map[String,String]): Unit = {
  try {
    val out = new BufferedOutputStream(new FileOutputStream("shrink.properties"))
    try {
      val props = new Properties()
      map.foreach { case(k,v) => props.setProperty(k, v) }
      props.store(out, "")
    } finally {
      out.close()
    }
  } catch {
    case _: IOException => //just ignore
  }
}

//at least shrinking combinators

def shrink(pluginPkg: String, ideaSdk: String, javaRt: String, proguard: String): Either[Error,String] = {
  for {
    _ <- ensureExists(pluginPkg).right
    out <- runProguard(proguard, pluginPkg, ideaSdk, javaRt).right
  } yield out
}

def ensureExists(f: String): Either[Error,String] = {
  if(new File(f).exists()) Right(f)
  else Left("File "+f+" not found")
}

def runProguard(proguard: String, inJars: String, ideaSdk: String, javaRt: String): Either[Error,String] = {
  import sys.process._

  val outJars = inJars.split("/").dropRight(1).mkString("/")+"/target/"+inJars.split("/").last.split("\\.").head+"-shrinked.zip"

  def escape(s: String) = "\""+s+"\""

  val cmd = Seq(
    "java", "-jar", escape(proguard), "@proguard.pro", "-injars", escape(inJars), "-outjars", escape(outJars),
    "-libraryjars", escape(javaRt)+";"+escape(ideaSdk)
  )

  val result = cmd !

  if(result == 0) Right(outJars)
  else Left("proguard error")
}

def askFor(prompt: String): Option[String] = {
  print(prompt+": ")
  Some(Console.readLine())
}

def tryFindOut(f: String): Either[Error,String] = {
  val fullPath = new File(".").getCanonicalPath + "/" + f

  ensureExists(fullPath)
}

def download(url: String): Either[Error,String] = {
  import sys.process._

  val out = new File("target/"+url.split("/").last)

  val result = new URL(url) #> out !

  if(result == 0) Right(out.getCanonicalPath)
  else Left("download url error: "+url)
}

val props = loadProps()

//ok, run all stuff

val result = for {
  pluginPkg <- tryFindOut("composer-json-plugin.zip").right
  ideaSdk <- props.get("ideaSdk").orElse(askFor("ideaSdk fullpath")).right
  javaRt <- props.get("javaRt").orElse(askFor("fullpath to rt.jar")).right
  proguard <- tryFindOut("target/proguard.jar").left.flatMap(_ => download(proguardUrl)).right
} yield {
  writeProps(Map("ideaSdk" -> ideaSdk, "javaRt" -> javaRt))
  shrink(pluginPkg, ideaSdk, javaRt, proguard)
}

//and make output

result.joinRight match {
  case Right(s) => println("file shrinked successfully: "+s)
  case Left(s) => Console.err.println("error: "+s); System.exit(1)
}