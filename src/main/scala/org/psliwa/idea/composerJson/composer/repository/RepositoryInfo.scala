package org.psliwa.idea.composerJson.composer.repository

case class RepositoryInfo(urls: List[String], packagist: Boolean, repository: Option[Repository[String]] = None)