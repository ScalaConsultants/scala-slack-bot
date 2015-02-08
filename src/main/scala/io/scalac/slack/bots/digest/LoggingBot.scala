package io.scalac.slack.bots.digest

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.SlackBot
import io.scalac.slack.common.{Incoming, UndefinedMessage}

/**
 * Created on 08.02.15 23:45
 */
class LoggingBot extends IncomingMessageListener{
  override def receive: Receive = {
    case UndefinedMessage(message) =>
      println("[LoggingBot] got message: " + message)
    case ignored => //nothing special
  }

}
