package org.psliwa.idea.composer.idea.completionContributor

import com.intellij.codeInsight.completion
import com.intellij.json.JsonLanguage
import org.psliwa.idea.composer.idea.{Keyword, CompletionContributor}

trait TestCase {
  def getCompletionContributor = {
    println(getCompletionContributors.head)
    getCompletionContributors.head
  }

  def getCompletionContributors = {
    import scala.collection.JavaConverters._

    completion.CompletionContributor.forLanguage(JsonLanguage.INSTANCE).asScala
      .filter(_.isInstanceOf[CompletionContributor])
      .map(_.asInstanceOf[CompletionContributor])
  }

  def setCompletionPackageLoader(f: () => Seq[Keyword]) = {
    getCompletionContributors.foreach(_.setPackagesLoader(f))
  }

  def setCompletionVersionsLoader(f: String => Seq[String]) = {
    getCompletionContributors.foreach(_.setVersionsLoader(f))
  }
}
