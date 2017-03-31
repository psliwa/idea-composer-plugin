package org.psliwa.idea.composerJson.util

import java.util
import java.util.Collections
import java.util.Map.Entry

object Funcs {
  def memorize[A,B](maxSize: Int)(f: A => B): A => B = {
    val cache = Collections.synchronizedMap(new util.LinkedHashMap[A,B](){
      override def removeEldestEntry(eldest: Entry[A, B]): Boolean = this.size() > maxSize
    })

    (a: A) => {
      val cachedValue = cache.get(a)
      if(cachedValue == null) {
        val newValue = f(a)
        cache.put(a, newValue)
        newValue
      } else {
        cachedValue
      }
    }
  }
}
