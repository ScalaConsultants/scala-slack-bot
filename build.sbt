name := "slack-scala-bot"

version := "1.0"

scalaVersion := "2.11.5"

organization := "io.scalac"

resolvers ++= Seq(
  // to get Datomisca
  "Pellucid Bintray"  at "http://dl.bintray.com/content/pellucid/maven",
  // to get Datomic free (for pro, you must put in your own repo or local)
  "clojars" at "https://clojars.org/repo"
)

libraryDependencies ++= {
  val akkaVersion = "2.3.9"
  Seq(
    "org.mockito" % "mockito-core" % "1.10.19",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "io.spray" %% "spray-json" % "1.3.1",
    "io.spray" %% "spray-client" % "1.3.1",
    "io.spray" %% "spray-can" % "1.3.2",
    "com.wandoulabs.akka" %% "spray-websocket" % "0.1.4",
    "joda-time" % "joda-time" % "2.7",
    "org.joda" % "joda-convert" % "1.7",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test",
    "log4j" % "log4j" % "1.2.17",
    "org.slf4j" % "slf4j-api" % "1.7.5",
    "org.slf4j" % "slf4j-log4j12" % "1.7.5",
    "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
    "org.scala-lang" % "scala-compiler" % "2.10.2",
    "org.scala-lang" % "jline" % "2.10.2",
    "org.twitter4j" % "twitter4j-core" % "4.0.0",
    "com.pellucid" %% "datomisca" % "0.7-alpha-11",
    "com.datomic" % "datomic-free" % "0.9.4724"
  )
}

resolvers += "spray repo" at "http://repo.spray.io"
