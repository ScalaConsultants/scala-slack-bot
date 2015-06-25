package io.scalac.slack.bots

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import io.scalac.slack.common._
import io.scalac.slack.{FindChannel, MessageEventBus, SlackBot}

import scala.concurrent.ExecutionContext


class DirectMessageTestBot extends IncomingMessageListener {
  log.debug(s"Starting $this")


  import context._

  override def receive: Receive = {
    case Command("direct", params, m) =>

      DirectMessage(m.user, "You said: " + params.mkString(" "))

  }

  override val bus: MessageEventBus = SlackBot.eventBus
}

object DirectMessage extends {

  import akka.pattern._

  implicit val timeout = Timeout(1, TimeUnit.SECONDS)

  def apply(key: String, message: String)(implicit context: ExecutionContext, forward: (OutboundMessage) => Unit): Unit = {
    SlackBot.userStorage ? FindChannel(key) onSuccess {
      case Some(channel: String) =>
        forward(OutboundMessage(channel, message))
    }

  }
}