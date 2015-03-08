package io.scalac.slack

import akka.actor.{Actor, ActorLogging, ActorRef}
import io.scalac.slack.common._

/**
 * Created on 08.02.15 23:00
 *
 */
class OutgoingRichMessageProcessor(apiActor: ActorRef) extends Actor with ActorLogging {

  override def receive: Receive = {

    case msg: RichOutboundMessage =>
      println("RICH MESSAGE ENCOUNTERED!!!")
      println(msg)
      apiActor ! ""

    case ignored => //nothing else

  }

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    SlackBot.eventBus.subscribe(self, Outgoing)
  }
}
