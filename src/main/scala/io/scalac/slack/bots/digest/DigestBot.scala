package io.scalac.slack.bots.digest

import java.io.FileWriter

import io.scalac.slack.bots.IncomingMessageListener
import io.scalac.slack.common.{Command, OutboundMessage}

import scala.io.Source
import scala.util.Try

class DigestBot extends IncomingMessageListener {

  println(s"Starting $this")

  val digestFile = "digest.txt"

  def put(linkToStore: String) = {
    val fw = new FileWriter(digestFile, true)
    try {
      fw.write(linkToStore)
      fw.write("\n")
    }
    finally fw.close()
  }
  def get() = Try { Source.fromFile(digestFile).getLines() }

  def receive = {
    case Command("digest-put", links) =>
      println(s"Got x= digest-put $links from Slack")
      links.foreach(put(_))
      publish(OutboundMessage("C03DN1GUJ", s"Link $links stored for Digest!"))

    case Command("digest-list", _) =>
      println(s"Got x= digest-get from Slack")
      get().map(d => publish(OutboundMessage("C03DN1GUJ", s"Digest contains: ${d.mkString(" ")}")) )

    case x => println(s"Digest ignores $x")
  }
}
