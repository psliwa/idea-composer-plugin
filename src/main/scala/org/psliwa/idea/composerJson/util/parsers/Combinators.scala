package org.psliwa.idea.composerJson.util.parsers

import scala.annotation.tailrec
import ImplicitConversions._

object Combinators {

  def succeed[A](a: A) : Parser[A] = input => Success(a,0)
  def fail[A](): Parser[A] = input => Failure

  def many[A](p: Parser[A]): Parser[List[A]] = map2(p, many(p))(_ :: _) or succeed(List[A]())
  def many1[A](p: Parser[A]): Parser[List[A]] = map2(p, many(p))(_::_)
  def map[A,B](p: Parser[A])(f: A => B): Parser[B] = flatMap(p)(a => succeed(f(a)))
  def flatMap[A,B](p: Parser[A])(f: A => Parser[B]): Parser[B] = loc => {
    p(loc) match {
      case Success(a,n) => f(a)(loc.advancedBy(n)).advanceSuccess(n)
      case Failure => Failure
    }
  }

  def or[A](p1: Parser[A], p2: => Parser[A]): Parser[A] = input => {
    p1(input) match {
      case Failure => p2(input)
      case x => x
    }
  }

  def map2[A,B,C](p1: Parser[A], p2: => Parser[B])(f: (A,B) => C): Parser[C] = {
    for {
      a <- p1
      b <- p2
    } yield f(a,b)
  }

  def slice[A](p: Parser[A]): Parser[String] = loc => {
    p(loc) match {
      case Success(a,n) => Success(loc.input.substring(0, n),n)
      case Failure => Failure
    }
  }

  def product[A,B](p1: Parser[A], p2: =>  Parser[B]): Parser[(A,B)] = {
    for {
      a <- p1
      b <- p2
    } yield (a,b)
  }

  def listOfN[A](n: Int, p: Parser[A]): Parser[List[A]] = {
    @tailrec
    def loop(n: Int, result: Parser[List[A]]): Parser[List[A]] = {
      if(n > 0) loop(n - 1, map2(p, result)(_ :: _))
      else result
    }

    loop(n, succeed(List[A]()))
  }

  def wrap[A](p: => Parser[A]): Parser[A] = p

  def run[A](p: Parser[A])(input: String): Option[A] = p(Location(input)) match {
    case Success(a,_) => Some(a)
    case Failure => None
  }
}
