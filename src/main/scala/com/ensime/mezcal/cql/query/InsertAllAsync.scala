package com.ensime.mezcal.cql.query

import scala.concurrent.{ ExecutionContext, Future }
import scala.jdk.CollectionConverters._

import com.datastax.driver.core.{ BatchStatement, ConsistencyLevel, ResultSet, Session }

import com.ensime.mezcal.cql.prepare.BatchPreparedStatement
import com.ensime.mezcal.interop.guava._
import com.ensime.mezcal.tracing.{ ExecutionInfo, QueryTracePrinter }

trait InsertAllAsync {

  implicit class InsertAllExecute(scene: BatchPreparedStatement) {

    def batch[A <: Product](
        idempotent: Boolean,
        level: ConsistencyLevel = ConsistencyLevel.LOCAL_QUORUM,
        tracing: () => Boolean = ExecutionInfo.DISABLE,
        printer: ResultSet => Unit = QueryTracePrinter
    )(implicit
        session: Session,
        executionContext: ExecutionContext
    ): A => Future[Boolean] = {

      val statements = scene.statements
      val batchType  = scene.batchType

      (params: A) =>
        val query = new BatchStatement(batchType)

        val allParams = params.productIterator
          .map(_.asInstanceOf[Product])
          .map(_.productIterator.toSeq.map(_.asInstanceOf[Object]))

        val allStatements = statements.zip(allParams).map {
          case (nextStatement, nextParams) => nextStatement.bind(nextParams: _*)
        }

        query.addAll(allStatements.asJava)
        query.setConsistencyLevel(level)
        query.setIdempotent(idempotent)

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
