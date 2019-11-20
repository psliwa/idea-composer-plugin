package org.psliwa.idea.composerJson.composer.model

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import org.psliwa.idea.composerJson.composer.InstalledPackages

import scala.util.matching.Regex

case class PackageDescriptor(name: PackageName,
                             version: String,
                             isDev: Boolean,
                             homepage: Option[String],
                             replacedBy: Option[PackageDescriptor])

object PackageDescriptor {
  def apply(name: String,
            version: String,
            isDev: Boolean = false,
            homepage: Option[String] = None,
            replacedBy: Option[PackageDescriptor] = None): PackageDescriptor = {
    PackageDescriptor(PackageName(name), version, isDev, homepage, replacedBy)
  }

  def documentationUrl(element: PsiElement, name: PackageName): Option[String] =
    documentationUrl(element.getContainingFile.getVirtualFile, name)

  def fixName(name: String): String = {
    def firstPackageLetter(m: Regex.Match) = m.start > 0 && name.charAt(m.start - 1) == '/'
    def dashAhead(m: Regex.Match) = m.start > 0 && name.charAt(m.start - 1) == '-'
    def firstVendorLetter(m: Regex.Match) = m.start == 0
    def letterPrefix(m: Regex.Match) = if (firstVendorLetter(m) || firstPackageLetter(m) || dashAhead(m)) "" else "-"

    "([A-Z])".r.replaceAllIn(name, (m: Regex.Match) => letterPrefix(m) + m.group(0).toLowerCase)
  }

  private def documentationUrl(composerJsonFile: VirtualFile, name: PackageName): Option[String] = {
    InstalledPackages.forFile(composerJsonFile).get(name).flatMap(_.homepage).orElse(packagistUrl(name))
  }

  private def packagistUrl(name: PackageName): Option[String] = {
    name.`vendor/project` match {
      case Some((vendor, project)) =>
        Some(s"https://packagist.org/packages/$vendor/$project")
      case None =>
        None
    }
  }
}
