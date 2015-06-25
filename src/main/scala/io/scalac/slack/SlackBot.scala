package io.scalac.slack

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import io.scalac.slack.api.{BotInfo, Start}
import io.scalac.slack.common.DefaultBotBundle
import io.scalac.slack.websockets.{WSActor, WebSocket}

object SlackBot {

  val system = ActorSystem("SlackBotSystem")

  val eventBus = new MessageEventBus

  val websocketClient = system.actorOf(Props(classOf[WSActor], eventBus), "ws-actor")
  val slackBot = system.actorOf(Props(classOf[SlackBotActor], new DefaultBotBundle, eventBus), "slack-bot")
  val userStorage = system.actorOf(Props[UsersStorage], "users-storage")

  var botInfo: Option[BotInfo] = None

  def main(args: Array[String]) {
    val logger = Logging(system, getClass)

    logger.info("SlackBot started")
    logger.debug("With api key: " + Config.apiKey)

    try {

      slackBot ! Start

      system.awaitTermination()
      logger.info("Shutdown successful...")

    } catch {
      case e: Exception =>
        logger.error("An unhandled exception occured...", e)
        system.shutdown()
        system.awaitTermination()
    }

  }

  sys.addShutdownHook(shutdown())

  def shutdown(): Unit = {
    websocketClient ! WebSocket.Release
    system.shutdown()
    system.awaitTermination()
  }

}
