package io.scalac.slack.bots.repl

import io.scalac.slack.bots.{AbstractBot, IncomingMessageListener}
import io.scalac.slack.common.{OutboundMessage, Command}

import scala.tools.nsc.interpreter.IMain
import scala.tools.nsc.Settings
import java.io.PrintWriter

class ReplBot(scalaLibraryPath: String) extends AbstractBot {

  log.debug(s"Starting $this")

  lazy val interpreter = new Repl(scalaLibraryPath)
  
  def act = {
    case Command("repl", code, message) =>
      log.debug(s"Got x= repl $code from Slack")
      val r = interpreter.run(code.mkString(" "))
      publish(OutboundMessage(message.channel, r))

    case Command("repl-reset", _, message) =>
      log.debug(s"Got x= repl-reset from Slack")
      interpreter.reset()
      publish(OutboundMessage(message.channel, "Repl reset"))
  }

  override def help(channel: String): OutboundMessage = OutboundMessage(channel,
    s"To sort-out Scala related discussions use *${name}* as a simple REPL \\n " +
      s"`repl code` - interpret the code \\n" +
      s"`repl-reset` - restart the repl ")
}

class Repl(scalaLibraryPath: String) {
  private val writer = new java.io.StringWriter()
  private val s = new Settings()
  s.bootclasspath.append(scalaLibraryPath)
  s.classpath.append(scalaLibraryPath)

  private def repl = new IMain(s, new PrintWriter(writer))

  def run(code: String): String = {
    writer.getBuffer.setLength(0)
    repl.interpret(code)
    writer.toString.replaceAll("\n", "")
  }

  def reset() = repl.reset()
}
