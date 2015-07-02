package io.scalac.slack.bots

import akka.util.Timeout
import io.scalac.slack.common._
import io.scalac.slack.{MessageEventBus, SlackBot}

import scala.concurrent.duration._
import scala.language.postfixOps


class DirectMessageTestBot(override val bus: MessageEventBus) extends IncomingMessageListener {
  log.debug(s"Starting $this")

  implicit val usersStorage = SlackBot.userStorage

  import context._

  implicit val timeOut: Timeout = 1 second

  override def receive: Receive = {
    case Command("direct", params, m) =>
      DirectMessage(m.user, "You said: " + params.mkString(" "))
  }
}

