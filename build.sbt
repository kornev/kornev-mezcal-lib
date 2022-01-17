import JavaOptions._
import ScalaOptions._
import Dependencies._

ThisBuild / organization := "com.ensime"
ThisBuild / scalafixDependencies ++= rules

lazy val root = (project in file("."))
  .settings( // main
    name := "mezcal",
    scalaVersion := "2.13.8",
    scalacOptions ++= scalaCompilerOptions,
    conflictManager := ConflictManager.strict,
    libraryDependencies ++= cassandra ::: tests,
    dependencyOverrides ++= overrides,
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
  .settings( // test
    fork in Test := true,
    javaOptions in Test ++= hotSpotOptions,
    logBuffered in Test := false,
    parallelExecution in Test := false
  )
