package com.ensime.mezcal.cql.parser

import com.datastax.driver.core.Row

trait RowParser[A] extends (Row => A) {

  def parse(row: Row): A
  def apply(row: Row): A = parse(row)
}
