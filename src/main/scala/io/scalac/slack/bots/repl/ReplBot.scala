package io.scalac.slack.bots.repl

import io.scalac.slack.bots.IncomingMessageListener
import io.scalac.slack.common.{OutboundMessage, Command}

import scala.tools.nsc.Interpreter
import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.interpreter.Results.Success

import scala.tools.nsc.interpreter.ILoop
import scala.tools.nsc.Settings
import java.io.CharArrayWriter
import java.io.PrintWriter

class ReplBot(scalaLibraryPath: String) extends IncomingMessageListener {

  log.debug(s"Starting $this")

  lazy val interpreter = new REPL(scalaLibraryPath)
  
  def receive = {
    case Command("repl", code, message) =>
      log.debug(s"Got x= repl $code from Slack")
      val r = interpreter.run(code.mkString(" "))
      publish(OutboundMessage(message.channel, r))
  }
}

class REPL(scalaLibraryPath: String) {

//  lazy val scalaLibraryPath = "/home/jfk/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.11.5.jar"

  def run(code: String) = {
    val writer = new java.io.StringWriter()
    val s = new Settings()
    s.bootclasspath.append(scalaLibraryPath)
    s.classpath.append(scalaLibraryPath)

    def repl = new IMain(s, new PrintWriter(writer))
    repl.interpret(code)
    repl.close()

    println("Done? YES! " + writer.toString)
    writer.toString.replaceAll("\n", "")
  }
}
