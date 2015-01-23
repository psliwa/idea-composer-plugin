package org.psliwa.idea.composerJson.reference

import javax.swing.Icon

import com.intellij.lang.{ASTNode, Language}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.{TextRange, Key}
import com.intellij.psi._
import com.intellij.psi.scope.PsiScopeProcessor
import com.intellij.psi.search.{SearchScope, GlobalSearchScope}

/**
 * Wrapper for PsiElement
 */
private class PsiElementWrapper(element: PsiElement) extends PsiElement {
  override def getProject: Project = element.getProject

  override def addRange(first: PsiElement, last: PsiElement): PsiElement = element.addRange(first, last)

  override def getOriginalElement: PsiElement = element.getOriginalElement

  override def textMatches(text: CharSequence): Boolean = element.textMatches(text)

  override def textMatches(element: PsiElement): Boolean = element.textMatches(element)

  override def putCopyableUserData[T](key: Key[T], value: T): Unit = element.putCopyableUserData(key, value)

  override def isPhysical: Boolean = element.isPhysical

  override def replace(newElement: PsiElement): PsiElement = element.replace(newElement)

  override def getCopyableUserData[T](key: Key[T]): T = element.getCopyableUserData(key)

  override def addAfter(element: PsiElement, anchor: PsiElement): PsiElement = element.addAfter(element, anchor)

  override def getPrevSibling: PsiElement = element.getPrevSibling

  override def getParent: PsiElement = element.getParent

  override def addBefore(element: PsiElement, anchor: PsiElement): PsiElement = element.addBefore(element, anchor)

  override def copy(): PsiElement = element.copy()

  override def getUseScope: SearchScope = element.getUseScope

  override def processDeclarations(processor: PsiScopeProcessor, state: ResolveState, lastParent: PsiElement,
    place: PsiElement): Boolean = element.processDeclarations(processor, state, lastParent, place)

  override def delete(): Unit = element.delete()

  override def findReferenceAt(offset: Int): PsiReference = element.findReferenceAt(offset)

  override def getContext: PsiElement = element.getContext

  override def accept(visitor: PsiElementVisitor): Unit = element.accept(visitor)

  override def deleteChildRange(first: PsiElement, last: PsiElement): Unit = element.deleteChildRange(first, last)

  override def getResolveScope: GlobalSearchScope = element.getResolveScope

  override def getTextLength: Int = element.getTextLength

  override def getNextSibling: PsiElement = element.getNextSibling

  override def textContains(c: Char): Boolean = element.textContains(c)

  override def acceptChildren(visitor: PsiElementVisitor): Unit = element.acceptChildren(visitor)

  override def findElementAt(offset: Int): PsiElement = element.findElementAt(offset)

  override def isWritable: Boolean = element.isWritable

  override def getManager: PsiManager = element.getManager

  override def checkDelete(): Unit = element.checkDelete()

  override def isValid: Boolean = element.isValid

  override def getTextRange: TextRange = element.getTextRange

  override def addRangeAfter(first: PsiElement, last: PsiElement, anchor: PsiElement): PsiElement = element.addRangeAfter(first, last, anchor)

  override def getContainingFile: PsiFile = element.getContainingFile

  override def getReferences: Array[PsiReference] = element.getReferences

  override def getLastChild: PsiElement = element.getLastChild

  override def checkAdd(element: PsiElement): Unit = element.checkAdd(element)

  override def getChildren: Array[PsiElement] = element.getChildren

  override def getText: String = element.getText

  override def getStartOffsetInParent: Int = element.getStartOffsetInParent

  override def add(element: PsiElement): PsiElement = element.add(element)

  override def getNode: ASTNode = element.getNode

  override def getLanguage: Language = element.getLanguage

  override def getTextOffset: Int = element.getTextOffset

  override def addRangeBefore(first: PsiElement, last: PsiElement, anchor: PsiElement): PsiElement = element.addRangeBefore(first, last, anchor)

  override def getNavigationElement: PsiElement = element.getNavigationElement

  override def getReference: PsiReference = element.getReference

  override def isEquivalentTo(another: PsiElement): Boolean = element.isEquivalentTo(another)

  override def textToCharArray(): Array[Char] = element.textToCharArray()

  override def getFirstChild: PsiElement = element.getFirstChild

  override def getIcon(flags: Int): Icon = element.getIcon(flags)

  override def putUserData[T](key: Key[T], value: T): Unit = element.putUserData(key, value)

  override def getUserData[T](key: Key[T]): T = element.getUserData(key)
}
