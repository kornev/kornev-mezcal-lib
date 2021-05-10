package com.ensime.mezcal.entities

import com.datastax.driver.core.{ Row, TypeCodec }

import com.ensime.mezcal.cql.parser.RowParser

case class User(id: Int, name: String, profession: String)

object User {

  implicit val parser: RowParser[User] = new RowParser[User] {

    def parse(row: Row): User =
      User(
        row.get("id", TypeCodec.cint()),
        row.get("name", TypeCodec.varchar()),
        row.get("job", TypeCodec.varchar())
      )
  }
}
