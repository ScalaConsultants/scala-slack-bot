package io.scalac.slack.common

import akka.actor.{ActorContext, ActorRef, Props}
import io.scalac.slack.bots.digest.DigestBot
import io.scalac.slack.bots.{CommandsRecognizerBot, LoggingBot, PingPongBot}

object BotModules {

  def registerModules(context: ActorContext, websocketClient: ActorRef) = {
    val loggingBot = context.actorOf(Props[LoggingBot])
    val pingpongBot = context.actorOf(Props[PingPongBot])
    val digestBot = context.actorOf(Props[DigestBot])
    val commandProcessor = context.actorOf(Props[CommandsRecognizerBot])
  }
}
