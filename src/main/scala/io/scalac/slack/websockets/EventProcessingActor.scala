package io.scalac.slack.websockets

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.common.UnspecifiedEvent

/**
 * Created on 06.02.15 20:32
 * Event processor get websocket message as string
 * and parse it to proper protocol and put it into SOURCE (eventbus)
 *
 */
class EventProcessingActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case message: String =>
      println("[EVENT_PROCESSOR] Message received: "+ message)
      context.system.eventStream.publish(UnspecifiedEvent(message))
    case ignored =>
  }
}
