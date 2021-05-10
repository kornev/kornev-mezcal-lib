package com.ensime.mezcal.cql.prepare

import com.datastax.driver.core.PreparedStatement

object SinglePreparedStatement {

  def apply(statement: PreparedStatement): SinglePreparedStatement =
    new SinglePreparedStatement(statement)
}

class SinglePreparedStatement(
    private[cql] val statement: PreparedStatement
)
