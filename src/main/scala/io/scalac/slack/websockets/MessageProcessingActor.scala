package io.scalac.slack.websockets

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Actor}

/**
 * Created on 06.02.15 20:32
 */
class MessageProcessingActor extends Actor with ActorLogging{
  override def receive: Receive = {
    case message: String =>

    case ignored =>
  }
}
