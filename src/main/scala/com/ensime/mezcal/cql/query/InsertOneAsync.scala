package com.ensime.mezcal.cql.query

import scala.concurrent.{ ExecutionContext, Future }

import com.datastax.driver.core.{ ConsistencyLevel, ResultSet, Session }

import com.ensime.mezcal.cql.prepare.SinglePreparedStatement
import com.ensime.mezcal.interop.guava._
import com.ensime.mezcal.tracing.{ ExecutionInfo, QueryTracePrinter }

trait InsertOneAsync {

  implicit class InsertOneExecute(scene: SinglePreparedStatement) {

    def insertOne[A <: Product](
        idempotent: Boolean,
        level: ConsistencyLevel = ConsistencyLevel.LOCAL_QUORUM,
        tracing: () => Boolean = ExecutionInfo.DISABLE,
        printer: ResultSet => Unit = QueryTracePrinter
    )(implicit
        session: Session,
        executionContext: ExecutionContext
    ): A => Future[Boolean] = {

      val blank = scene.statement
        .setConsistencyLevel(level)

      (params: A) =>
        val query = blank
          .bind(params.productIterator.toSeq.map(_.asInstanceOf[Object]): _*)
          .setIdempotent(idempotent)

        if (tracing())
          query.enableTracing()

        for {
          rs <- session.executeAsync(query).toFuture
        } yield {
          if (query.isTracing) printer(rs)
          rs.wasApplied
        }
    }
  }
}
