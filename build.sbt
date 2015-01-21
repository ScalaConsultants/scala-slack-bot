name := "slack_api"

version := "1.0"

scalaVersion := "2.11.5"

organization := "io.scalac"

val akkaVersion = "2.3.8"

libraryDependencies ++= Seq(
  "org.mockito" % "mockito-core" % "1.10.19",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "io.spray" % "spray-json_2.11" % "1.3.1",
  "io.spray" % "spray-client" % "1.3.1",
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "log4j" % "log4j" % "1.2.17",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-log4j12" % "1.7.5"
)