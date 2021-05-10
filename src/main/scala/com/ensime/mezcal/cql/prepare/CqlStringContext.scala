package com.ensime.mezcal.cql.prepare

import com.datastax.driver.core.{ Session, SimpleStatement }

trait CqlStringContext {

  implicit class QueryPrepare(stringContext: StringContext) {

    def cql(args: Any*)(implicit session: Session): SinglePreparedStatement =
      SinglePreparedStatement(
        session.prepare(new SimpleStatement(stringContext.raw(args: _*)))
      )
  }
}
