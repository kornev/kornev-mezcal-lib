package com.ensime.mezcal.cql.prepare

import com.datastax.driver.core.{ BatchStatement, PreparedStatement }

object BatchPreparedStatement {

  def apply(
      batchType: BatchStatement.Type,
      statements: List[PreparedStatement]
  ): BatchPreparedStatement =
    new BatchPreparedStatement(batchType, statements)
}

class BatchPreparedStatement(
    private[cql] val batchType: BatchStatement.Type,
    private[cql] val statements: List[PreparedStatement]
)
