import sbt._

object Dependencies {

  val cassandra = {
    val `DRIVER`              = "3.11.0"
    val `NETTY`               = "4.1.73.Final"
    val `DROPWIZARD`          = "4.1.23"
    val `GUAVA`               = "31.0.1-jre"
    val `JACKSON-DATABIND`    = "2.7.9.7"
    val `JACKSON-ANNOTATIONS` = "2.7.9"
    val `JNR-FFI`             = "2.1.16"
    val `JNR-POSIX`           = "3.0.61"
    val `HDR-HISTOGRAM`       = "2.1.12"
    val `SLF4J`               = "1.7.32"

    val driver = List(
      "com.datastax.cassandra" % "cassandra-driver-core",
      "com.datastax.cassandra" % "cassandra-driver-extras"
    ) map (_ % `DRIVER` excludeAll (
      ExclusionRule("io.dropwizard.metrics", "metrics-core"),
      ExclusionRule("io.netty", "netty-buffer"),
      ExclusionRule("io.netty", "netty-transport"),
      ExclusionRule("io.netty", "netty-codec"),
      ExclusionRule("io.netty", "netty-handler"),
      ExclusionRule("io.netty", "netty-common"),
      ExclusionRule("com.google.guava", "guava"),
      ExclusionRule("com.fasterxml.jackson.core", "jackson-databind"),
      ExclusionRule("com.github.jnr", "jnr-ffi"),
      ExclusionRule("com.github.jnr", "jnr-posix"),
      ExclusionRule("org.slf4j", "slf4j-api")
    ))

    val netty = List(
      "io.netty" % "netty-buffer",
      "io.netty" % "netty-transport",
      "io.netty" % "netty-codec",
      "io.netty" % "netty-handler",
      "io.netty" % "netty-common"
    ) map (_ % `NETTY`)

    val metrics = List(
      "io.dropwizard.metrics" % "metrics-core",
      "io.dropwizard.metrics" % "metrics-jmx"
    ) map (_ % `DROPWIZARD` exclude ("org.slf4j", "slf4j-api"))

    val other = List(
      "com.google.guava"           % "guava"               % `GUAVA`,
      "com.github.jnr"             % "jnr-ffi"             % `JNR-FFI`,
      "com.github.jnr"             % "jnr-posix"           % `JNR-POSIX`,
      "com.fasterxml.jackson.core" % "jackson-databind"    % `JACKSON-DATABIND` exclude ("com.fasterxml.jackson.core", "jackson-annotations"),
      "com.fasterxml.jackson.core" % "jackson-annotations" % `JACKSON-ANNOTATIONS`,
      "org.hdrhistogram"           % "HdrHistogram"        % `HDR-HISTOGRAM`,
      "org.slf4j"                  % "slf4j-api"           % `SLF4J`
    )

    driver ::: netty ::: metrics ::: other
  }

  val overrides = Nil

  val rules = {
    val `ORGANIZE-IMPORTS` = "0.5.0"

    List(
      "com.github.liancheng" %% "organize-imports" % `ORGANIZE-IMPORTS`
    )
  }

  val tests = {
    val `EMBEDDED-CASSANDRA` = "4.0.6"
    val `LOG4J`              = "1.2.17"
    val `SLF4J`              = "1.7.32"
    val `SCALATEST`          = "3.2.10"

    List(
      "com.github.nosan" % "embedded-cassandra" % `EMBEDDED-CASSANDRA` % Test,
      "log4j"            % "log4j"              % `LOG4J`              % Test,
      "org.slf4j"        % "slf4j-log4j12"      % `SLF4J`              % Test,
      "org.scalatest"   %% "scalatest"          % `SCALATEST`          % Test
    )
  }
}
