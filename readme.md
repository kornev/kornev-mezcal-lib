# Mezcal

Mezcal is a lightweight, fast Cassandra database access layer for Scala keeping complete control over the CQL.

## Using Mezcal

`SinglePreparedStatement.scala` created using the custom [string interpolation method]("https://docs.scala-lang.org/overviews/core/string-interpolation.html#advanced-usage") cql.
 Method `selectOne` in `SelectOneAsync.scala` make a  function with closure.

```scala
val getUser: (Tuple2[Int, String]) => Future[User] =
  cql"SELECT * FROM profiles WHERE id = ? AND name = ?"
    .selectOne[(Int, String), User](tracing = ExecutionInfo.ENABLE)

getUser(30, "Tervo").foreach(println)
```

Write the trace query information to the log output if tracing is enabled (as in the query above).

```text
21/05/09 21:26:24 [INFO] [QueryTracePrinter] Tracing session: 0f905c80-b0f4-11eb-8753-f9c5ada0271a
Execute: SELECT * FROM users WHERE id = ? AND name = ?;
Bound: id = 30;
Bound: name = 'Tervo';
Per-statement: consistency_level = LOCAL_QUORUM;
Per-statement: serial_consistency_level = SERIAL;
Per-statement: page_size = 1;

------------------------------------------------------------------------+---------------+----------------+-------------
 activity                                                               | timestamp     | source         | elapsed time
------------------------------------------------------------------------+---------------+----------------+-------------
                                            Execute CQL3 prepared query | 1620558695579 | /192.168.7.142 |            0
                              Executing single-partition query on users | 1620558695579 | /192.168.7.142 |          135
                                           Acquiring sstable references | 1620558695579 | /192.168.7.142 |          251
                                              Merging memtable contents | 1620558695579 | /192.168.7.142 |          283
                                 Read 1 live rows and 0 tombstone cells | 1620558695579 | /192.168.7.142 |          350
                                                       Request complete |             - |              - |          406
------------------------------------------------------------------------+---------------+----------------+-------------
```

See `QueryAsyncSpec.scala` for documentation.

## Prerequisites

* Java 8, 11
* Scala 2.13
* Datastax Java Driver 3.11.0
* Logging backend compatible with SLF4J

A compatible logging backend is [Logback](http://logback.qos.ch), add it to your sbt build definition:

```scala
val overrides = {
  val `SLF4J` = "1.7.32"

  List(
    "org.slf4j" % "slf4j-api" % `SLF4J`
  )
}

val commons = {
  val `LOGBACK`       = "1.2.4"
  val `SCALA-LOGGING` = "3.9.4"

  List(
    "ch.qos.logback"              % "logback-classic" % `LOGBACK`,
    "com.typesafe.scala-logging" %% "scala-logging"   % `SCALA-LOGGING` exclude ("org.slf4j", "slf4j-api")
  )
}
```

## Getting Mezcal

SBT users may add this to their `Dependencies.scala`:

```scala
val commons = {
  val `MEZCAL` = "0.4"
  val `NETTY`  = "4.1.66.Final"
  
  val driver = List(
    "com.ensime" %% "mezcal" % `MEZCAL`
  )
  
  val epoll =
    if (sys.props("os.name") == "Linux")
      List("io.netty" % "netty-transport-native-epoll" % `NETTY` classifier "linux-x86_64")
    else
      Nil
  
  driver ::: epoll
}
```

## License

Copyright (C) 2021 Vadim Kornev.  
Distributed under the MIT License.
