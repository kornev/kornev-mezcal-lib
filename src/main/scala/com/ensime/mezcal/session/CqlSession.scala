package com.ensime.mezcal.session

import java.io.Closeable

import scala.concurrent.{ ExecutionContext, Future }

import com.datastax.driver.core.{
  PreparedStatement,
  RegularStatement,
  ResultSet,
  Session,
  Statement
}

import com.ensime.mezcal.interop.guava._

object CqlSession {

  def apply(session: Session): CqlSession = new CqlSession(session)
}

class CqlSession(session: Session) extends Closeable {

  // format: off
  def prepare(statement: RegularStatement): PreparedStatement =
    session.prepare(statement)
    
  def execute(statement: Statement)(implicit ec: ExecutionContext): ResultSet =
    session.execute(statement)

  def prepareAsync(statement: RegularStatement)(implicit ec: ExecutionContext): Future[PreparedStatement] =
    session.prepareAsync(statement).toFuture

  def executeAsync(statement: Statement)(implicit ec: ExecutionContext): Future[ResultSet] =
    session.executeAsync(statement).toFuture
  // format: on

  def close(): Unit =
    session.close()
}
