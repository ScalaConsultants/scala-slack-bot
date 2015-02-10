package io.scalac.slack.bots.digest

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.scalac.slack.bots.IncomingMessageListener
import io.scalac.slack.common.{OutboundMessage, Ping}
import io.scalac.slack.websockets.WebSocket

class DigestBot extends IncomingMessageListener {

//  import context.dispatcher

  println(s"Starting $this")

  def receive = {
    case x =>
      println(s"Got x= $x from Slack")
      publish(OutboundMessage("C03DN1GUJ", "Digest!"))
  }
}
