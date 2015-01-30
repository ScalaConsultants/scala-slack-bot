package io.scalac.slack.bots.digest

import akka.actor.{ActorRef, ActorLogging, Actor}
import io.scalac.slack.websockets.WebSocket

class DigestBotActor(ws: ActorRef) extends Actor with ActorLogging {

  def receive = {
    case x =>
      log.debug(s"Got x= $x from Slack")
      ws ! WebSocket.Send("just testing here")
  }
}
