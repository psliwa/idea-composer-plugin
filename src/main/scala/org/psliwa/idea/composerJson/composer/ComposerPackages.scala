package org.psliwa.idea.composerJson.composer

case class ComposerPackages(packages: Map[String,PackageDescriptor]) {
  def get(name: String): Option[PackageDescriptor] = packages.get(name)
  def descriptors: List[PackageDescriptor] = packages.values.toList
  def isEmpty: Boolean = packages.isEmpty
  def nonEmpty: Boolean = packages.nonEmpty
}

object ComposerPackages {
  def apply(packages: PackageDescriptor*): ComposerPackages =
    ComposerPackages(packages.map(pkg => pkg.name.toLowerCase -> pkg).toMap)
}