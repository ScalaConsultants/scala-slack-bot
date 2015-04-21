package io.scalac.slack.bots.ping

import java.util.concurrent.TimeUnit

import io.scalac.slack.{SlackBot, MessageEventBus}
import io.scalac.slack.bots.IncomingMessageListener
import io.scalac.slack.common.{Ping, Pong}

import scala.concurrent.duration.Duration

/**
 * Created on 09.02.15 00:02
 */
class PingPongBot extends IncomingMessageListener {

  import context.dispatcher

  override def receive: Receive = {
    case Pong =>
      sendPing()
    case Ping =>
      publish(Ping)
  }

  def sendPing() = {
    context.system.scheduler.scheduleOnce(Duration.create(30, TimeUnit.SECONDS), self, Ping)
  }

  @throws[Exception](classOf[Exception]) override
  def preStart(): Unit = {
   sendPing()
    super.preStart()
  }

  override def bus: MessageEventBus = SlackBot.eventBus
}
