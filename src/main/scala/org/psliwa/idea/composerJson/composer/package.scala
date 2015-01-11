package org.psliwa.idea.composerJson

package object composer {
  type Package = (String,String)
  type Packages = Map[String,String]

  object Package {
    def apply(name: String, version: String): Package = (name,version)
  }

  object Packages {
    def apply(packages: Package*): Packages = Map[String,String](packages:_*)
  }
}
