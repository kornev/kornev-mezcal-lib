package com.ensime.mezcal.cql.prepare

import com.datastax.driver.core.BatchStatement

trait BatchContext {

  def counter(statements: SinglePreparedStatement*): BatchPreparedStatement =
    BatchPreparedStatement(
      BatchStatement.Type.COUNTER,
      statements.map(_.statement).toList
    )

  def logged(statements: SinglePreparedStatement*): BatchPreparedStatement =
    BatchPreparedStatement(
      BatchStatement.Type.LOGGED,
      statements.map(_.statement).toList
    )

  def unlogged(statements: SinglePreparedStatement*): BatchPreparedStatement =
    BatchPreparedStatement(
      BatchStatement.Type.UNLOGGED,
      statements.map(_.statement).toList
    )
}
