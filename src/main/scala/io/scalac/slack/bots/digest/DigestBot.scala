package io.scalac.slack.bots.digest

import java.io.FileWriter

import io.scalac.slack.bots.IncomingMessageListener
import io.scalac.slack.common.{Command, OutboundMessage}
import io.scalac.slack.bots.IncomingMessageListener
import io.scalac.slack.common._

import scala.io.Source
import scala.util.Try

class DigestBot extends IncomingMessageListener {

  log.debug(s"Starting $this")

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
    case Command("digest-put", links, _) =>
      log.debug(s"Got x= digest-put $links from Slack")
      links.foreach(put(_))
      publish(OutboundMessage("C03DN1GUJ", s"Link $links stored for Digest!"))

    case Command("digest-list", _, _) =>
      log.debug(s"Got x= digest-get from Slack")
      get().map(d => publish(OutboundMessage("C03DN1GUJ", s"Digest contains: ${d.mkString(" ")}")) )

  }
}
