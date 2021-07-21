package com.ensime.mezcal.cluster

import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

import com.datastax.driver.core.policies.TokenAwarePolicy.ReplicaOrdering
import com.datastax.driver.core.policies.{
  DCAwareRoundRobinPolicy,
  HostFilterPolicy,
  LatencyAwarePolicy,
  TokenAwarePolicy
}
import com.datastax.driver.core.{
  Cluster,
  ConsistencyLevel,
  Host,
  PlainTextAuthProvider,
  QueryOptions,
  SocketOptions
}

object ClusterSettings {

  def contactPoints(config: NodesConfig)(builder: Cluster.Builder): Unit =
    builder.addContactPointsWithPorts(
      config.collect { case (address, port, _, _) => new InetSocketAddress(address, port) }: _*
    )

  // SEE: https://datastax-oss.atlassian.net/browse/JAVA-1448
  def localDcLoadBalancing(localDc: String)(builder: Cluster.Builder): Unit = {
    val roundRobinWithFilterPolicy = new HostFilterPolicy(
      DCAwareRoundRobinPolicy.builder.withLocalDc(localDc).build(),
      (_: Host).getDatacenter == localDc
    )
    val tokenAwarePolicy = new TokenAwarePolicy(
      roundRobinWithFilterPolicy,
      ReplicaOrdering.RANDOM
    )
    val latencyAwarePolicy = LatencyAwarePolicy
      .builder(tokenAwarePolicy)
      .withExclusionThreshold(1.2)
      .withScale(25, TimeUnit.MILLISECONDS)
      .withRetryPeriod(5, TimeUnit.SECONDS)
      .withUpdateRate(100, TimeUnit.MILLISECONDS)
      .withMininumMeasurements(50)
      .build()

    builder.withLoadBalancingPolicy(latencyAwarePolicy)
  }

  def readTimeout100(builder: Cluster.Builder): Unit =
    builder.withSocketOptions(
      (new SocketOptions).setReadTimeoutMillis(100)
    )

  def plainTextAuthProvider(config: NodesConfig)(builder: Cluster.Builder): Unit =
    // format: off
    builder.withAuthProvider(
      config
        .take(1)
        .collect { case (_, _, username, password) => new PlainTextAuthProvider(username, password) }
        .head
    )
    // format: on

  def localQuorumConsistencyLevel(builder: Cluster.Builder): Unit =
    builder.withQueryOptions(
      (new QueryOptions).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
    )
}
