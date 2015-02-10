package io.scalac.slack

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.common.UndefinedMessage

/**
 * Created on 08.02.15 23:36
 * Incomming message processor should parse incoming string
 * and change into proper protocol
 */
class IncomingMessageProcessor(implicit eventBus: MessageEventBus) extends Actor with ActorLogging {
  override def receive: Receive = {
    case s: String =>
      //TODO: Parse to protocol message
      eventBus.publish(UndefinedMessage(s))
    case ignored => //nothing special
  }
}
