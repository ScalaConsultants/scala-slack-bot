package io.scalac.slack.bots.digest

import java.io.{File, FileWriter}

import io.scalac.slack.common.{Command, OutboundMessage}
import io.scalac.slack.bots.IncomingMessageListener

import scala.io.Source
import scala.util.Try

class DigestBot extends IncomingMessageListener {

  log.debug(s"Starting $this")

  val digestFile = "digest.txt"

  def put(linkToStore: String) = {
    val fw = new FileWriter(digestFile, true)
    try {
      fw.append(linkToStore)
      fw.append("\n")
    }
    finally fw.close()
  }
  def get() = Try { Source.fromFile(digestFile).getLines() }
  def clear() = {
    new File(digestFile).delete()
    put("")
  }

  def receive = {
    case Command("digest-put", links, message) =>
      log.debug(s"Got x= digest-put $links from Slack")
      links.foreach(put(_))
      publish(OutboundMessage(message.channel, s"Link $links stored for Digest!"))

    case Command("digest-list", _, message) =>
      log.debug(s"Got x= digest-list from Slack")
      publishContent(message.channel)

    case Command("digest-clear", _, message) =>
      log.debug(s"Got x= digest-clear from Slack")
      publishContent(message.channel)
      publish(OutboundMessage(message.channel, s"Now Clearing"))
      clear()
  }
  
  def publishContent(channel: String): Unit = {
    get().map(d => publish(OutboundMessage(channel, s"Digest contained: ${d.mkString(" ")}")))
  }
}
