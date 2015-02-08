package io.scalac.slack.common

import akka.actor.{ActorContext, ActorRef, Props}
import io.scalac.slack.bots.digest.LoggingActor

object BotModules {

  def registerModules(context: ActorContext, websocketClient: ActorRef) = {
    val loggerBot = context.actorOf(Props[LoggingActor], "loggin-bot")
    context.system.eventStream.subscribe(loggerBot, classOf[UnspecifiedEvent])


  }
}
