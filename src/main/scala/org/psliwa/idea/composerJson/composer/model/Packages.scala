package org.psliwa.idea.composerJson.composer.model

case class Packages private (packages: Map[String, PackageDescriptor]) {
  def get(name: PackageName): Option[PackageDescriptor] = packages.get(name.presentation.toLowerCase)
  def descriptors: List[PackageDescriptor] = packages.values.toList
  def isEmpty: Boolean = packages.isEmpty
  def nonEmpty: Boolean = packages.nonEmpty
}

object Packages {
  def apply(packages: PackageDescriptor*): Packages =
    Packages(packages.map(pkg => pkg.name.presentation.toLowerCase -> pkg).toMap)
}
