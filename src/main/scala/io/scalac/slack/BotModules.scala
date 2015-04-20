package io.scalac.slack

import akka.actor.{ActorRef, ActorContext}

trait BotModules {
  def registerModules(context: ActorContext, websocketClient: ActorRef): Unit
}
