package org.psliwa.idea.composerJson.composer.model

case class PackageName(presentation: String) {
  private val parts = presentation.split('/').toList

  val vendor: Option[String] = parts match {
    case List(vendor, _) => Some(vendor)
    case _ => None
  }

  val project: String = parts.last

  def `vendor/project`: Option[(String, String)] = vendor.map(_ -> project)
}
