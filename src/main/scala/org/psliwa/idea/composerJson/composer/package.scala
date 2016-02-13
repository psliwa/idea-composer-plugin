package org.psliwa.idea.composerJson

package object composer {
  type ComposerPackages = Map[String,ComposerPackage]

  object ComposerPackages {
    def apply(packages: ComposerPackage*): ComposerPackages = packages.map(pkg => pkg.name.toLowerCase -> pkg).toMap
  }
}