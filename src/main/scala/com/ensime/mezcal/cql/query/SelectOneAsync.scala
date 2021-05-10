package com.ensime.mezcal.cql.query

import scala.concurrent.{ ExecutionContext, Future }

import com.datastax.driver.core.{ ConsistencyLevel, ResultSet, Session }

import com.ensime.mezcal.cql.parser.RowParser
import com.ensime.mezcal.cql.prepare.SinglePreparedStatement
import com.ensime.mezcal.interop.guava._
import com.ensime.mezcal.tracing.{ ExecutionInfo, QueryTracePrinter }

trait SelectOneAsync {

  implicit class SelectOneExecute(scene: SinglePreparedStatement) {

    def selectOne[A <: Product, B: RowParser](
        level: ConsistencyLevel = ConsistencyLevel.LOCAL_QUORUM,
        tracing: () => Boolean = ExecutionInfo.DISABLE,
        printer: ResultSet => Unit = QueryTracePrinter
    )(implicit
        session: Session,
        executionContext: ExecutionContext
    ): A => Future[B] = {

      val blank = scene.statement
        .setConsistencyLevel(level)

      (params: A) =>
        val query = blank
          .bind(params.productIterator.toSeq.map(_.asInstanceOf[Object]): _*)
          .setIdempotent(true)
          .setFetchSize(1)

        if (tracing())
          query.enableTracing()

        for {
          rs <- session.executeAsync(query).toFuture
        } yield {
          if (query.isTracing) printer(rs)
          implicitly[RowParser[B]].parse(rs.one())
        }
    }
  }
}
