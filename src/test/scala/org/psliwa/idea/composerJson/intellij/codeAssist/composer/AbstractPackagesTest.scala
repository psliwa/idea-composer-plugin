package org.psliwa.idea.composerJson.intellij.codeAssist.composer

import com.intellij.codeInsight
import com.intellij.json.JsonLanguage
import org.psliwa.idea.composerJson.composer.model.PackageName
import org.psliwa.idea.composerJson.intellij.codeAssist.{BaseLookupElement, CompletionTest}

abstract class AbstractPackagesTest extends CompletionTest {

  def getCompletionContributor = {
    getCompletionContributors.head
  }

  def getCompletionContributors = {
    import scala.collection.JavaConverters._

    codeInsight.completion.CompletionContributor.forLanguage(JsonLanguage.INSTANCE).asScala
      .filter(_.isInstanceOf[CompletionContributor])
      .map(_.asInstanceOf[CompletionContributor])
  }

  def setCompletionPackageLoader(f: () => Seq[BaseLookupElement]): Unit = {
    getCompletionContributors.foreach(_.setPackagesLoader(f))
  }

  def setCompletionVersionsLoader(f: String => Seq[String]): Unit = {
    getCompletionContributors.foreach(_.setVersionsLoader(f.compose[PackageName](_.presentation)))
  }
}
