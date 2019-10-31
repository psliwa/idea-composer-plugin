package org.psliwa.idea.composerJson

package object composer {
  type ComposerPackages = Map[String,PackageDescriptor]

  object ComposerPackages {
    def apply(packages: PackageDescriptor*): ComposerPackages = packages.map(pkg => pkg.name.toLowerCase -> pkg).toMap
  }
}