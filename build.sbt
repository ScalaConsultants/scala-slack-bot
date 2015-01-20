name := "slack_api"

version := "1.0"

scalaVersion := "2.11.5"

organization := "io.scalac"

libraryDependencies ++= Seq(
  "org.mockito" % "mockito-core" % "1.10.19",
  "com.typesafe.akka" %% "akka-actor" % "2.3.8",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.8",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.8" % "test",
  "org.json4s" %% "json4s-native" % "3.2.11",
  "org.scalaj" %% "scalaj-http" % "1.1.0",
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "log4j" % "log4j" % "1.2.17",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-log4j12" % "1.7.5"
)