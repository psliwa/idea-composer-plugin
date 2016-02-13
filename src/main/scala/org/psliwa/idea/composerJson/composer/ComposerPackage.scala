package org.psliwa.idea.composerJson.composer

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement

case class ComposerPackage(name: String, version: String, isDev: Boolean = false, homepage: Option[String] = None)

object ComposerPackage {
  def `vendor/package`(s: String): Option[(String, String)] = {
    s.split("/") match {
      case Array(vendor, pkg) => Some((vendor, pkg))
      case _ => None
    }
  }

  def documentationUrl(element: PsiElement, pkg: String): String = documentationUrl(element.getContainingFile.getVirtualFile, pkg)

  private def documentationUrl(composerJsonFile: VirtualFile, pkg: String): String = {
    InstalledPackages.forFile(composerJsonFile).get(pkg).flatMap(_.homepage).getOrElse(packagistUrl(pkg))
  }

  private def packagistUrl(pkg: String) = s"https://packagist.org/packages/$pkg"
}
