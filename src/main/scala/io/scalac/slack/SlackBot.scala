package io.scalac.slack

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import io.scalac.slack.actors.BotActor
import io.scalac.slack.actors.messages.Start

/**
 * Created on 20.01.15 21:51
 */
object SlackBot {

  def main(args: Array[String]) {
    val system = ActorSystem("SlackBotSystem")
    val logger = Logging(system, getClass)

    logger.info("SlackBot started")
    logger.debug("With api key: " + Config.apiKey)

    try {

      system.actorOf(Props[BotActor]) ! Start

      system.awaitTermination()
      logger.info("Shutdown successful...")

    } catch {
      case e: Exception =>
        logger.error("An unhandled exception occured...", e)
        system.shutdown()
        system.awaitTermination()
    }

  }

}
