package com.ensime.mezcal.interop

import java.util.concurrent.Executor

import scala.concurrent.{ ExecutionContext, Future, Promise }

import com.google.common.util.concurrent.{ FutureCallback, Futures, ListenableFuture }

trait Guava {

  implicit class ListenableFutureAdapter[A](future: ListenableFuture[A]) {

    def toFuture(implicit ec: ExecutionContext): Future[A] = {

      val promise = Promise[A]()
      val callback = new FutureCallback[A] {
        def onFailure(t: Throwable): Unit = promise.failure(t)
        def onSuccess(value: A): Unit     = promise.success(value)
      }
      Futures.addCallback(future, callback, ec.asInstanceOf[Executor])
      promise.future
    }
  }
}
