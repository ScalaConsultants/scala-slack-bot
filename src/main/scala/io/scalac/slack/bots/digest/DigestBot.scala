package io.scalac.slack.bots.digest

import io.scalac.slack.bots.IncomingMessageListener
import io.scalac.slack.common._

class DigestBot extends IncomingMessageListener {


  println(s"Starting $this")

  var storage = List("link 1", "link 2")

  def put(linkToStore: String) = storage = linkToStore :: storage

  def get() = storage

  def receive = {
    case Command("digest-put", link :: _, message) =>
      println(s"Got x= digest-put $link from Slack")
      put(link)
      publish(OutboundMessage(message.channel, s"Link $link stored for Digest!"))

    case Command("digest-get", _, message) =>
      println(s"Got x= digest-get from Slack")
      publish(OutboundMessage(message.channel, s"Digest contains: ${get().mkString(" ")}"))
  }
}
