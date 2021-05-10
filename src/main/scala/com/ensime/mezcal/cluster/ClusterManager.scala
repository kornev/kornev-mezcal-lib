package com.ensime.mezcal.cluster

import java.net.InetSocketAddress

import scala.sys.{ addShutdownHook => shutdown }

import com.codahale.metrics.jmx.JmxReporter
import com.datastax.driver.core.{ Cluster, ConsistencyLevel, PlainTextAuthProvider, QueryOptions }
import com.datastax.driver.extras.codecs.jdk8.{ LocalDateCodec, LocalTimeCodec }

object ClusterManager {

  def make(config: ClusterConfig): Cluster = {

    val cluster =
      Cluster.builder
        .addContactPointsWithPorts(
          config.collect { case (address, port, _, _) => new InetSocketAddress(address, port) }: _*
        )
        .withAuthProvider(
          config.collect {
            case (_, _, username, password) => new PlainTextAuthProvider(username, password)
          } head
        )
        .withQueryOptions((new QueryOptions).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM))
        .withoutJMXReporting()
        .build

    cluster.getConfiguration.getCodecRegistry
      .register(LocalTimeCodec.instance)
      .register(LocalDateCodec.instance)

    cluster.init()

    val jmx = JmxReporter
      .forRegistry(cluster.getMetrics.getRegistry)
      .inDomain(s"${cluster.getClusterName}-metrics")
      .build

    jmx.start()

    shutdown {
      jmx.close()
      cluster.close()
    }

    cluster
  }
}
