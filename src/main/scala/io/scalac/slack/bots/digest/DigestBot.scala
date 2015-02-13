package io.scalac.slack.bots.digest

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.scalac.slack.bots.IncomingMessageListener
import io.scalac.slack.common._
import io.scalac.slack.websockets.WebSocket

class DigestBot extends IncomingMessageListener {


  println(s"Starting $this")

  var storage = List("link 1", "link 2")

  def put(linkToStore: String) = storage = linkToStore :: storage
  def get() = storage

  def receive = {
    case Command("digest-put", link :: _ , _) =>
      println(s"Got x= digest-put $link from Slack")
      put(link)
      publish(OutboundMessage("C03DN1GUJ", s"Link $link stored for Digest!"))

    case Command("digest-get", _ , _) =>
      println(s"Got x= digest-get from Slack")
      publish(OutboundMessage("C03DN1GUJ", s"Digest contains: ${get().mkString(" ")}"))

    case x => println(s"Digest ignores $x")
  }
}
