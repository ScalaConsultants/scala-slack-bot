package io.scalac.slack.bots.digest

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.scalac.slack.bots.IncomingMessageListener
import io.scalac.slack.common.{DirectMessage, OutboundMessage, Ping}
import io.scalac.slack.websockets.WebSocket

class DigestBot extends IncomingMessageListener {

//  import context.dispatcher

  println(s"Starting $this")

  var storage = List("link 1", "link 2")

  def isStoreCommand(s: String) = {
    println(s"IS $s store? ${s.startsWith("$digest-put")}")
    s.startsWith("$digest-put")
  }
  def isShowAllCommand(s: String) = s.startsWith("$digest-get")

  def put(linkToStore: String) = storage = linkToStore :: storage
  def get() = storage

  def receive = {
    case DirectMessage(txt) if isStoreCommand(txt) =>
      println(s"Got x= $txt from Slack")
      val link = txt.split(" ")(1)
      put(link)
      publish(OutboundMessage("C03DN1GUJ", s"Link $link stored for Digest!"))

    case DirectMessage(txt) if isShowAllCommand(txt) =>
      println(s"Got x= $txt from Slack")
      publish(OutboundMessage("C03DN1GUJ", s"Digest contains: ${get().mkString(" ")}"))

    case x => println(s"Digest ignores $x")
  }
}
