package com.ensime.mezcal.tracing

import scala.jdk.CollectionConverters._

import com.datastax.driver.core.ResultSet
import org.slf4j.LoggerFactory

object QueryTracePrinter extends ((ResultSet) => Unit) {

  private lazy val logger = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))

  def apply(rs: ResultSet): Unit = {
    val writer          = new StringBuilder()
    val queryTrace      = rs.getExecutionInfo.getQueryTrace
    val queryParameters = queryTrace.getParameters.asScala

    writer.append(s"Tracing session: ${queryTrace.getTraceId}\n")

    if (queryParameters.contains("query")) {
      writer.append(s"""Execute: ${queryParameters("query")}; """)
      writer.append(
        queryParameters
          .filter(_._1.startsWith("bound_var"))
          .values
          .mkString("[", ", ", "]")
      )
      writer.append("\n")
    }
    writer.append("\n")

    writer.append(s"""${"-" * 163}+${"-" * 15}+${"-" * 16}+${"-" * 13}\n""")
    writer.append(
      " %-161s | %-13s | %-14s | %-12s\n"
        .format("activity", "timestamp", "source", "elapsed time")
    )
    writer.append(s"""${"-" * 163}+${"-" * 15}+${"-" * 16}+${"-" * 13}\n""")
    writer.append(
      "%162s | %12s | %10s | %12s\n"
        .format(
          queryTrace.getRequestType,
          queryTrace.getStartedAt,
          queryTrace.getCoordinator,
          "0"
        )
    )
    for (event <- queryTrace.getEvents.asScala) {
      val (head, rest) = {
        val lines = event.getDescription
          .split("\n")
          .map(_.trim)
          .map(s => " " + s)
          .mkString
          .trim
          .replace(" columns=", "\ncolumns=")
          .replace(" Row[", "\nRow[")
          .replace(" Commit(", "\n")
          .replace("])", "]")
          .replace("EMPTY | ", "EMPTY\n")
          .split("\n")
          .toList

        lines.head -> lines.tail
      }

      writer.append(
        "%162s | %12s | %10s | %12s\n"
          .format(
            head,
            event.getTimestamp,
            event.getSource,
            event.getSourceElapsedMicros
          )
      )
      rest.map { next =>
        writer.append(
          "%162s | %13s | %14s | %12s\n"
            .format(next, "-", "-", "-")
        )
      }
    }
    writer.append(
      "%162s | %13s | %14s | %12s\n"
        .format("Request complete", "-", "-", queryTrace.getDurationMicros)
    )
    writer.append(s"""${"-" * 163}+${"-" * 15}+${"-" * 16}+${"-" * 13}""")

    logger.info(writer.result())
  }
}
