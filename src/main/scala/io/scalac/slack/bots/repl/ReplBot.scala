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

class ReplBot extends IncomingMessageListener {

  log.debug(s"Starting $this")

  def receive = {
    case Command("repl", code, message) =>
      log.debug(s"Got x= repl $code from Slack")
      val r = REPL.run(code.mkString(" "))
      publish(OutboundMessage(message.channel, r))
  }
}

object REPL {

  def run(code: String) = {

    //    def repl = new ILoop {
    //      override def loop(): Unit = {
    //        params.foreach(p => intp.bind(p._1, p._2, p._3))
    ////        super.loop()
    //        val result = super.command("1+2")
    //        super.echo("TEST " + result)
    //        super.closeInterpreter()
    //      }
    //    }

//    val pw = new PrintWriter(System.out, true)
//
//    val settings = new Settings
//    settings.Yreplsync.value = true
//    settings.usejavacp.value = true
////    settings.usejavacp.value = false
//    // Different settings needed when running from SBT or normally
//    //    if (isRunFromSBT) {
//    //      settings.embeddedDefaults[ReplBot.type]
//    //    } else {
//    //      settings.usejavacp.value = true
//    //    }
//
//    def repl = new IMain(settings, pw)
//    println(" ==== created ==== ")
//    //    repl.process(settings)
//    repl.ensureClassLoader()
//    //    println("repl.definedTypes " + repl.definedTypes)
////    println("repl.definedTypes " + repl.parse("22 + 11"))
////    repl.initializeSynchronous()
//    println(" ==== INIT Done!! ==== ")
//    repl.interpret("22+11")
//    //    repl.interpret("\"string var\"")
//    println(" ==== Done!! ==== ")

    val s = new Settings()
    val scalaLibraryPath = "/home/jfk/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.11.5.jar"
    s.bootclasspath.append(scalaLibraryPath)
    s.classpath.append(scalaLibraryPath)

    val writer = new java.io.StringWriter()
    val n = new Interpreter(s, new PrintWriter(writer))
    writer.getBuffer.setLength(0)
    val result = n.interpret(code)
    n.close()
    println("Done? YES! " + writer.toString)
    result match {
      case Success => writer.toString.replaceAll("\n", " ")
      case _ => "Bot Error"
    }
  }
}
