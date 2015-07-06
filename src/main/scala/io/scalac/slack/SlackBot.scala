package io.scalac.slack

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import io.scalac.slack.api.Start
import io.scalac.slack.common.actors.SlackBotActor
import io.scalac.slack.common.{DefaultBotBundle, Shutdownable, UsersStorage}
import io.scalac.slack.websockets.WebSocket

object SlackBot extends Shutdownable {

  val system = ActorSystem("SlackBotSystem")

  val eventBus = new MessageEventBus

  val userStorage = system.actorOf(Props[UsersStorage], "users-storage")
  val slackBot = system.actorOf(Props(classOf[SlackBotActor], new DefaultBotBundle, eventBus, this, Some(userStorage)), "slack-bot")

  def main(args: Array[String]) {
    val logger = Logging(system, getClass)

    logger.info("SlackBot has been started")
    logger.debug("With api key: " + Config.apiKey)

    try {

      slackBot ! Start

      system.awaitTermination()
      logger.info("Shutdown successful...")

    } catch {
      case e: Exception =>
        logger.error("An unhandled exception occured...", e)
        shutdown()
    }

  }

  sys.addShutdownHook(shutdown())

  def shutdown(): Unit = {
    slackBot ! WebSocket.Release
    system.shutdown()
    system.awaitTermination()
  }

}
