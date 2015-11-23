package org.psliwa.idea.composerJson.composer.version

case class SemanticVersion(major: Int, private val other: Option[(Int,Option[(Int)])]) {
  val minor: Option[Int] = other.map(_._1)
  val patch: Option[Int] = other.flatMap(_._2)
  private val maxPartsNumber: Int = 3

  private[this] lazy val parts = List(Some(major), minor, patch).takeWhile(_.isDefined).map(_.get)
  private[this] lazy val reversedParts = parts.reverse

  val partsNumber = parts.size

  ensure(major >= 0)
  minor.foreach(i => ensure(i >= 0))
  patch.foreach(i => ensure(i >= 0))

  def this(major: Int) = {
    this(major, None)
  }

  def this(major: Int, minor: Int) = {
    this(major, Some(minor, None))
  }

  def this(major: Int, minor: Int, patch: Int) = {
    this(major, Some(minor, Some(patch)))
  }

  def this(versions: Array[Int]) = {
    this(SemanticVersion.getOrThrow(versions)(0), SemanticVersion.getOther(versions))
  }

  def incrementLast: SemanticVersion = {
    new SemanticVersion(((reversedParts.head+1) :: reversedParts.tail).reverse.toArray)
  }

  def dropLast: Option[SemanticVersion] = {
    if(reversedParts.size == 1) None
    else Some(new SemanticVersion(reversedParts.tail.reverse.toArray))
  }

  def append(part: Int) = {
    if(reversedParts.size == maxPartsNumber) None
    else Some(new SemanticVersion((part :: reversedParts).reverse.toArray))
  }

  def fillZero: SemanticVersion = ensureParts(maxPartsNumber)

  def ensureParts(i: Int): SemanticVersion = {
    ensure(i <= maxPartsNumber && i > 0)

    if(parts.size >= i) this
    else new SemanticVersion((parts.toList ++ List.fill(i - parts.size)(0)).toArray)
  }

  def ensureExactlyParts(i: Int): SemanticVersion = {
    ensure(i <= maxPartsNumber && i > 0)

    if(parts.size == i) this
    else if(parts.size >= i) new SemanticVersion(parts.take(i).toArray)
    else ensureParts(i)
  }

  def dropZeros: SemanticVersion = {
    new SemanticVersion(reversedParts.dropWhile(_ == 0).reverse.toArray)
  }

  override def toString: String = {
    def partToString(p: Option[Int]) = p.map("."+_).getOrElse("")

    ""+major+partToString(minor)+partToString(patch)
  }

  private def ensure(b: Boolean): Unit = if(!b) throw new IllegalArgumentException
}

private object SemanticVersion {
  private def tryGet[A](as: Array[A])(index: Int): Option[A] = if(index >= as.length) None else Option(as(index))
  private def getOrThrow[A](as: Array[A])(index: Int): A = if(index >= as.length) throw new IllegalArgumentException else as(index)

  private def getOther(versions: Array[Int]): Option[(Int,Option[Int])] = {
    val getVersionPart = tryGet(versions) _

    getVersionPart(1).map(minor => (minor, getVersionPart(2)))
  }
}
