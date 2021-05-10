package com.ensime.mezcal.cql.query

import scala.concurrent.{ ExecutionContext, Future }
import scala.jdk.CollectionConverters._

import com.datastax.driver.core.{ ConsistencyLevel, ResultSet, Session }

import com.ensime.mezcal.cql.parser.RowParser
import com.ensime.mezcal.cql.prepare.SinglePreparedStatement
import com.ensime.mezcal.interop.guava._
import com.ensime.mezcal.tracing.{ ExecutionInfo, QueryTracePrinter }

trait SelectAllAsync {

  implicit class SelectAllExecute(scene: SinglePreparedStatement) {

    def selectAll[A <: Product, B: RowParser](
        fetchSize: Int,
        level: ConsistencyLevel = ConsistencyLevel.LOCAL_QUORUM,
        tracing: () => Boolean = ExecutionInfo.DISABLE,
        printer: ResultSet => Unit = QueryTracePrinter
    )(implicit
        session: Session,
        executionContext: ExecutionContext
    ): A => Future[Iterator[B]] = {

      val blank = scene.statement
        .setConsistencyLevel(level)

      (params: A) =>
        val query = blank
          .bind(params.productIterator.toSeq.map(_.asInstanceOf[Object]): _*)
          .setIdempotent(true)
          .setFetchSize(fetchSize)

        if (tracing())
          query.enableTracing()

        for {
          rs <- session.executeAsync(query).toFuture
        } yield {
          val parse = implicitly[RowParser[B]]
          rs.iterator.asScala.map { row =>
            if (query.isTracing && rs.getAvailableWithoutFetching == 0)
              printer(rs)
            parse(row)
          }
        }
    }
  }
}
