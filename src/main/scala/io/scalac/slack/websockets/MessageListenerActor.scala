package io.scalac.slack.websockets

import akka.actor.{ActorRef, Actor, ActorLogging}
import io.scalac.slack.common.{Message, Ping}

/**
 * Created on 08.02.15 10:59
 *
 * Message listener listen to event bus for messages, parse it into string
 * and send by websocket
 */
class MessageListenerActor(wsSource: ActorRef) extends Actor with ActorLogging{
  override def receive: Receive = {
    case Ping =>
      println("[MLA] PING")
      wsSource ! WebSocket.Send(Ping.toString)
    case m: Message =>
      println("[MLA] MESSAGE")
      wsSource ! WebSocket.Send(m.toString)
    case ignored =>
      println("read ignored message")


  }
}
