object ScalaOptions {

  // format: off
  val scalaCompilerOptions = List(
    "-encoding", "utf8",
    "-target:jvm-1.8",
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
    "-language:existentials",
    "-language:higherKinds",
    "-language:postfixOps",
    "-Ywarn-unused:imports"
  )
  // format: on
}
