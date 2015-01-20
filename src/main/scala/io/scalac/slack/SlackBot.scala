package io.scalac.slack

import akka.actor.{ActorSystem, Props}
import io.scalac.slack.actors.BotActor
import io.scalac.slack.actors.messages.Start

/**
 * Created on 20.01.15 21:51
 */
object SlackBot extends Logger {

  def main(args: Array[String]) {
    logger.info("SlackBot started")
    logger.debug("With api key: " + Config.apiKey)


    val system = ActorSystem("SlackBotSystem")
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
