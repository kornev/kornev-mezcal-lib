package com.ensime.mezcal.tracing

import scala.collection.immutable.TreeMap
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

import com.datastax.driver.core.ResultSet
import org.slf4j.LoggerFactory

object QueryTracePrinter extends ((ResultSet) => Unit) {
  private final val SPLIT_TABLE_FORMAT  = "%s+%s+%s+%s%n"
  private final val HEADER_TABLE_FORMAT = " %-161s | %-13s | %-14s | %-12s%n"
  private final val ROW_TABLE_FORMAT    = "%162s | %13s | %14s | %12s%n"

  private lazy val SPLIT_TABLE =
    SPLIT_TABLE_FORMAT.format("-" * 163, "-" * 15, "-" * 16, "-" * 13)

  private lazy val HEADER_TABLE =
    HEADER_TABLE_FORMAT.format("activity", "timestamp", "source", "elapsed time")

  private lazy val logger = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))

  // format: off
  private lazy val parseBoundKey = {
    val re = """bound_var_(\d*)_(\w*)""".r
    val findAll = (pattern: Regex, source: String) =>
      for (m <- pattern.findAllIn(source).matchData) yield m.group(1) -> m.group(2)

    source: String =>
      findAll(re, source).next()
  }
  // format: on

  def apply(rs: ResultSet): Unit = {
    val queryWriter     = new StringBuilder()
    val queryTrace      = rs.getExecutionInfo.getQueryTrace
    val queryParameters = queryTrace.getParameters.asScala

    queryWriter.append(s"Tracing session: ${queryTrace.getTraceId}\n")

    queryParameters.collect { case ("query", v) => v } foreach { query =>
      queryWriter.append(s"Execute: ${query};\n")
    }

    queryParameters.filter { case (k, _) => k.startsWith("bound_var") }.collect {
      case (key, value) =>
        val (id, name) = parseBoundKey(key)
        id.toInt -> (name -> value)
    }.to(TreeMap).values.foreach {
      case (name, value) =>
        val preparedValue = if (value.length > 64) s"${value.substring(0, 64)}..." else value
        queryWriter.append(s"Bound: ${name} = ${preparedValue};\n")
    }

    queryParameters.filterNot {
      case (k, _) => k.startsWith("bound_var") || k.startsWith("query")
    }.foreach {
      case (key, value) => queryWriter.append(s"Per-statement: ${key} = ${value};\n")
    }

    queryWriter.append("\n")

    queryWriter.append(SPLIT_TABLE)
    queryWriter.append(HEADER_TABLE)
    queryWriter.append(SPLIT_TABLE)

    // format: off
    queryWriter.append(
      ROW_TABLE_FORMAT.format(queryTrace.getRequestType, queryTrace.getStartedAt, queryTrace.getCoordinator, "0")
    )
    // format: on

    for (event <- queryTrace.getEvents.asScala) {
      val (head, rest) = {
        val lines = event.getDescription.split("\n").map(_.trim)
        lines.head -> lines.tail
      }

      // format: off
      queryWriter.append(
        ROW_TABLE_FORMAT.format(head, event.getTimestamp, event.getSource, event.getSourceElapsedMicros)
      )
      // format: on

      rest.foreach(next =>
        queryWriter.append(
          ROW_TABLE_FORMAT.format(next, "-", "-", "-")
        )
      )
    }

    queryWriter.append(
      ROW_TABLE_FORMAT.format("Request complete", "-", "-", queryTrace.getDurationMicros)
    )

    queryWriter.append(SPLIT_TABLE)

    logger.info(queryWriter.result())
  }
}
