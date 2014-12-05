package org.psliwa.idea.composer.parser

sealed trait Schema

case class SObject(children: Map[String, Schema]) extends Schema
case class SArray(child: Schema) extends Schema
case class SStringChoice(choices: List[String]) extends Schema
case class SOr(left: Schema, right: Schema) extends Schema

object SBoolean extends Schema
object SString extends Schema
object SNumber extends Schema
