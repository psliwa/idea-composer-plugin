package org.psliwa.idea.composerJson.composer.version

case class SemanticVersion(major: Int, private val other: Option[(Int,Option[(Int,Option[Int])])]) {
  val minor: Option[Int] = other.map(_._1)
  val patch: Option[Int] = other.flatMap(_._2.map(_._1))
  val minorPatch: Option[Int] = other.flatMap(_._2.flatMap(_._2))

  ensure(major >= 0)
  minor.foreach(i => ensure(i >= 0))
  patch.foreach(i => ensure(i >= 0))
  minorPatch.foreach(i => ensure(i >= 0))

  def this(major: Int) = {
    this(major, None)
  }

  def this(major: Int, minor: Int) = {
    this(major, Some(minor, None))
  }

  def this(major: Int, minor: Int, patch: Int) = {
    this(major, Some(minor, Some(patch, None)))
  }

  def this(major: Int, minor: Int, patch: Int, minorPatch: Int) = {
    this(major, Some(minor, Some(patch, Some(minorPatch))))
  }

  def this(versions: Array[Int]) = {
    this(SemanticVersion.getOrThrow(versions)(0), SemanticVersion.getOther(versions))
  }

  override def toString: String = {
    def partToString(p: Option[Int]) = p.map("."+_).getOrElse("")

    ""+major+partToString(minor)+partToString(patch)+partToString(minorPatch)
  }

  private def ensure(b: Boolean): Unit = if(!b) throw new IllegalArgumentException
}

private object SemanticVersion {
  private def tryGet[A](as: Array[A])(index: Int): Option[A] = if(index >= as.length) None else Option(as(index))
  private def getOrThrow[A](as: Array[A])(index: Int): A = if(index >= as.length) throw new IllegalArgumentException else as(index)

  private def getOther(versions: Array[Int]): Option[(Int,Option[(Int,Option[Int])])] = {
    val getVersionPart = tryGet(versions) _

    getVersionPart(1).map(minor => (minor, getVersionPart(2).map(patch => (patch, getVersionPart(3)))))
  }
}
