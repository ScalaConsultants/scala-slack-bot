package io.scalac.slack

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.scalac.slack.common.{MessageCounter, Outgoing, Ping, SlackDateTime}
import io.scalac.slack.websockets.WebSocket

/**
 * Created on 08.02.15 23:00
 * Outgoing message protocol should change received
 * protocol into string and send it to websocket
 */
class OutgoingMessageProcessor(wsActor: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {
    case Ping =>
      val pingString = s"""{"id": ${MessageCounter.next}, "type": "ping","time": ${SlackDateTime.timeStamp}}"""
      wsActor ! WebSocket.Send(pingString)

    case ignored => //nothing else

  }

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    SlackBot.eventBus.subscribe(self, Outgoing)
  }
}
