package io.scalac.slack.bots.digest

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.common.UnspecifiedEvent

/**
 * Created on 08.02.15 11:42
 */
class LoggingActor extends Actor with ActorLogging {
  override def receive: Receive = {

    case UnspecifiedEvent(eventMessage) =>
      println("[LOG] GOT MESSAGE: " + eventMessage)
    case ignored =>
      log.warning("I DON'T KNOW WHAT IS IT: " + ignored.toString)
  }

}
