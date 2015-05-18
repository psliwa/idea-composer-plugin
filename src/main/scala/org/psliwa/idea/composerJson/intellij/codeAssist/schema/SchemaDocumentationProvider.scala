package org.psliwa.idea.composerJson.intellij.codeAssist.schema

import java.util

import com.intellij.lang.documentation.DocumentationProvider
import com.intellij.psi.{PsiManager, PsiElement}
import com.intellij.patterns.PlatformPatterns._
import org.psliwa.idea.composerJson.json.{SArray, SObject, Property, Schema}
import org.psliwa.idea.composerJson._

class SchemaDocumentationProvider extends DocumentationProvider {
  import SchemaDocumentationProvider._

  private val schema = ComposerSchema

  override def getQuickNavigateInfo(element: PsiElement, originalElement: PsiElement): String = null

  override def getDocumentationElementForLookupItem(psiManager: PsiManager, `object`: scala.Any,
    element: PsiElement): PsiElement = null

  override def getDocumentationElementForLink(psiManager: PsiManager, link: String,
    context: PsiElement): PsiElement = null

  override def getUrlFor(element: PsiElement, originalElement: PsiElement): util.List[String] = {
    ensureInValidFile(originalElement).map(
      findTokens(_) match {
        case PropertyToken(name)::_ => util.Arrays.asList("https://getcomposer.org/doc/04-schema.md#"+name)
        case _ => null
      }
    ).orNull
  }

  private def ensureInValidFile(element: PsiElement): Option[PsiElement] = {
    if(pattern.accepts(element)) {
      Some(element)
    } else {
      None
    }
  }

  override def generateDoc(element: PsiElement, originalElement: PsiElement): String = {
    ensureInValidFile(originalElement).flatMap(
      findProperty(_).map(_.description)
    ).orNull
  }

  private def findProperty(element: PsiElement): Option[Property] = {
    val tokens = findTokens(element)

    def loop(tokens: List[SchemaToken], schema: Schema): Option[Property] = {
      schema match {
        case SObject(properties, _) => {
          tokens match {
            case PropertyToken(name)::Nil => properties.get(name)
            case PropertyToken(name)::t => properties.get(name).map(_.schema).flatMap(loop(t, _))
            case _ => None
          }
        }
        case SArray(child) => {
          tokens match {
            case ArrayToken()::t => loop(t, child)
            case _ => None
          }
        }
        case _ => None
      }
    }

    schema.flatMap(loop(tokens, _))
  }

  private def findTokens(element: PsiElement): List[SchemaToken] = {
    import org.psliwa.idea.composerJson.intellij.PsiExtractors._

    def loop(element: PsiElement, tokens: List[SchemaToken]): List[SchemaToken] = {
      element match {
        case JsonArray(_) => loop(element.getParent, ArrayToken()::tokens)
        case JsonProperty(name, _) => loop(element.getParent, PropertyToken(name)::tokens)
        case JsonFile(_) => tokens
        case _ => loop(element.getParent, tokens)
      }
    }

    loop(element, List())
  }
}

private object SchemaDocumentationProvider {
  import com.intellij.json.psi.JsonFile

  val pattern = psiElement().inFile(psiFile(classOf[JsonFile]).withName(ComposerJson))

  private sealed trait SchemaToken
  private case class PropertyToken(name: String) extends SchemaToken
  private case class ArrayToken() extends SchemaToken
}
