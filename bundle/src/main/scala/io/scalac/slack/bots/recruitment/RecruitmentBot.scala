package io.scalac.slack.bots.recruitment

import akka.actor.Actor.Receive
import akka.actor.Props
import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.OutboundMessage

/**
 * Maintainer: Patryk
 */
class RecruitmentBot(override val bus: MessageEventBus, asanaKey: String, httpClient: AbstractHttpClient) extends AbstractBot {

  val asana = context.actorOf(
    Props(
      classOf[AsanaEventsActor],
      asanaKey, httpClient, context.system
    ), "asana-client"
  )

  asana ! StartObserving(1000)

  override def act: Receive = {
    case msg => println(s" ===== $msg")
  }

  override def help(channel: String): OutboundMessage =
    OutboundMessage(channel, s"Round Robin for Asana Recruitment tasks")
}
