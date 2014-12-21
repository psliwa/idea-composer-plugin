package org.psliwa.idea.composerJson.util

import java.util
import java.util.Map.Entry

object Funcs {
  def memorize[A,B](maxSize: Int)(f: A => B): A => B = {
    val cache = new util.LinkedHashMap[A,B](){
      override def removeEldestEntry(eldest: Entry[A, B]): Boolean = this.size() > maxSize
    }

    (a: A) => {
      if(!cache.containsKey(a)) cache.put(a, f(a))
      cache.get(a)
    }
  }
}
