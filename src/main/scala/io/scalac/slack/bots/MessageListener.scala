package io.scalac.slack.bots

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.SlackBot
import io.scalac.slack.common._

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

abstract class AbstractBot extends IncomingMessageListener {
  log.debug(s"Starting ${self.path.name}")

  def name: String = self.path.name

  def help(channel: String): OutboundMessage

  def act: Actor.Receive

  def handleSystemCommands: Actor.Receive = {
    case HelpRequest(t, ch) if t.map(_ == name).getOrElse(true) => publish(help(ch))
  }

  override final def receive: Actor.Receive = act.orElse(handleSystemCommands)
}
