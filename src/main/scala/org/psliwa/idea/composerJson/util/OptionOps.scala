package org.psliwa.idea.composerJson.util

object OptionOps {
  def traverseMap[K,A,B](as: Map[K,A])(f: A => Option[B]): Option[Map[K,B]] = {
    as.foldLeft(Option(Map[K, B]()))((obs, ka) => map2(f(ka._2), obs)((b, m) => m + (ka._1 -> b)))
  }

  def traverse[A,B](as: List[A])(f: A => Option[B]): Option[List[B]] = {
    as.foldLeft(Option(List[B]()))((obs, a) => map2(obs, f(a))(_ :+ _))
  }

  def map2[A,B,C](o1: Option[A], o2: Option[B])(f: (A,B) => C) = o1.flatMap(a => o2.map(b => f(a, b)))
}
