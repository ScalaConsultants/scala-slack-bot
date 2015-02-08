package io.scalac.slack.common

import akka.actor.{ActorContext, ActorRef, Props}
import io.scalac.slack.bots.digest.{LoggingBot, PingPongBot}

object BotModules {

  def registerModules(context: ActorContext, websocketClient: ActorRef) = {
    val loggingBot = context.actorOf(Props[LoggingBot])
    val pingpongbot = context.actorOf(Props[PingPongBot])
  }
}
