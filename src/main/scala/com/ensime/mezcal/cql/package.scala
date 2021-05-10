package com.ensime.mezcal

import com.ensime.mezcal.cql.prepare.{ BatchContext, CqlStringContext }
import com.ensime.mezcal.cql.query.{
  InsertAllAsync,
  InsertOneAsync,
  SelectAllAsync,
  SelectOneAsync
}

package object cql
  extends CqlStringContext
     with BatchContext
     with InsertOneAsync
     with InsertAllAsync
     with SelectOneAsync
     with SelectAllAsync {}
