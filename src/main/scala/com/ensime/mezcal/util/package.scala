package com.ensime.mezcal

import java.io.Closeable
import scala.sys.{ addShutdownHook => shutdown }

package object util {

  implicit class WithShutdownMethod[A <: Closeable](closeable: A) {

    def withShutdown(): A = {
      shutdown { closeable.close() }
      closeable
    }
  }
}
