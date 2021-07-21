package com.ensime.mezcal.cluster

import scala.sys.{ addShutdownHook => shutdown }

import com.codahale.metrics.jmx.JmxReporter
import com.datastax.driver.core.Cluster
import com.datastax.driver.extras.codecs.jdk8.{ LocalDateCodec, LocalTimeCodec }

object ClusterManager {

  def make(config: NodesConfig, localDc: String = "datacenter1"): Cluster = {
    import ClusterSettings._

    make(
      contactPoints(config),
      localDcLoadBalancing(localDc),
      readTimeout100,
      plainTextAuthProvider(config),
      localQuorumConsistencyLevel
    )
  }

  def make(settings: (Cluster.Builder => Unit)*): Cluster = {

    val cluster =
      settings
        .foldLeft(Cluster.builder) { case (st, fn) => fn(st); st }
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
