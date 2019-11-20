package org.psliwa.idea.composerJson.composer.model.repository

case class RepositoryInfo(urls: List[String], packagist: Boolean, repository: Option[Repository[String]] = None)
