package com.ensime.mezcal.util

object Tuple0 {

  def apply(): Tuple0 = new Tuple0()
}

class Tuple0 extends AnyRef with Product {

  def productArity: Int            = 0
  def productElement(n: Int): Any  = throw new IllegalStateException("No element")
  def canEqual(that: Any): Boolean = false
}
