name := "slack-scala-bot"

version := "1.0"

scalaVersion := "2.11.5"

organization := "io.scalac"

lazy val core = project.in(file("core"))

lazy val bundle = project.in(file("bundle")).dependsOn(core)
