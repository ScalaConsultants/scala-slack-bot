package io.scalac.slack.common

import akka.actor.{ActorRef, ActorContext, Props}
import io.scalac.slack.bots.digest.DigestBotActor
import io.scalac.slack.websockets.WebSocket
object BotModules {

  def registerModules(context: ActorContext, websocketClient: ActorRef) = {
//    List(
//      context.actorOf(Props(classOf[DigestBotActor], websocketClient), "digest-bot")
//    ).foreach(module => websocketClient ! WebSocket.RegisterModule(module) )
  }
}
