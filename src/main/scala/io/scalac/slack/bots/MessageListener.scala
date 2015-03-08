package io.scalac.slack.bots

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.SlackBot
import io.scalac.slack.common.{RichOutboundMessage, Outgoing, Incoming, MessageEvent}

/**
 * Created on 08.02.15 23:52
 */
trait MessagePublisher {
  def bus = SlackBot.eventBus

  def publish(event: MessageEvent) = {
    bus.publish(event)
  }
  def publish(event: RichOutboundMessage) = {
    bus.publish(event)
  }
}

abstract class MessageListener extends Actor with ActorLogging with MessagePublisher

abstract class IncomingMessageListener extends MessageListener {
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = bus.subscribe(self, Incoming)
}

abstract class OutgoingMessageListener extends MessageListener {
  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = bus.subscribe(self, Outgoing)
}
