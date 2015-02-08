package io.scalac.slack.bots.digest

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.SlackBot
import io.scalac.slack.common.{Outgoing, Incoming, MessageEvent}

/**
 * Created on 08.02.15 23:52
 */
abstract class MessageListener extends Actor with ActorLogging {

  def bus = SlackBot.eventBus

  def publish(event: MessageEvent) = {
    bus.publish(event)
  }
}

abstract class IncomingMessageListener extends MessageListener {
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = bus.subscribe(self, Incoming)
}

abstract class OutgoingMessageListener extends MessageListener {
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = bus.subscribe(self, Outgoing)
}
