package org.psliwa.idea.composerJson

package object composer {
  case class Package(name: String, version: String, isDev: Boolean = false)
  type Packages = Map[String,Package]

  object Packages {
    def apply(packages: Package*): Packages = Map[String,Package](packages.map(pkg => pkg.name.toLowerCase -> pkg):_*)
  }
}
