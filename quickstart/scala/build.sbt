val scala3Version = "3.5.1"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-example",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    libraryDependencies += "io.littlehorse" % "littlehorse-client" % "0.11.2",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.4.11",
      "org.slf4j" % "slf4j-api" % "2.0.9"
    )
  )
