package org.psliwa.idea.composerJson.intellij.codeAssist.schema

import com.intellij.codeInsight.completion._
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.patterns.PlatformPatterns._
import org.psliwa.idea.composerJson.intellij.codeAssist._
import org.psliwa.idea.composerJson.intellij.codeAssist.{AbstractCompletionContributor, BaseLookupElement}
import org.psliwa.idea.composerJson.json._

import scala.annotation.tailrec

class CompletionContributor extends AbstractCompletionContributor {

  import AbstractCompletionContributor._

  override protected def getCompletionProvidersForSchema(s: Schema, parent: Capture): List[(Capture, CompletionProvider[CompletionParameters])] = s match {
    case SStringChoice(m) => List((psiElement().withSuperParent(2, parent), new LookupElementsCompletionProvider(() => m.map(new BaseLookupElement(_)))))
    case _ => List()
  }

  override protected def propertyCompletionProvider(parent: Capture, properties: Map[String, Property]) = {
    propertyCompletionProvider(parent, () => properties.map(x => new BaseLookupElement(x._1, description = x._2.description)), (k) => insertHandlerFor(properties.get(k.name).get.schema))
  }

  @tailrec
  override final protected def insertHandlerFor(schema: Schema): Option[InsertHandler[LookupElement]] = schema match {
    case SString(_) | SStringChoice(_) | SFilePath(_) => Some(StringPropertyValueInsertHandler)
    case SObject(_, _) | SPackages | SFilePaths(_) => Some(ObjectPropertyValueInsertHandler)
    case SArray(_) => Some(ArrayPropertyValueInsertHandler)
    case SBoolean | SNumber => Some(EmptyPropertyValueInsertHandler)
    case SOr(h::_) => insertHandlerFor(h)
    case _ => None
  }
}