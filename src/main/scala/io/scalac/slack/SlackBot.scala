package io.scalac.slack

import akka.actor.ActorSystem

/**
 * Created on 20.01.15 21:51
 */
object SlackBot extends Logger {

  def main(args: Array[String]) {
    logger.info("SlackBot started")
    logger.debug("With api key: " + Config.apiKey)


    val system = ActorSystem("SlackBotSystem")
    try {


      system.awaitTermination()
      logger.info("Shutdown successful...")

    }catch {
      case e: Exception =>
        logger.error("An unhandled exception occured...", e)
        system.shutdown()
        system.awaitTermination()
    }

  }

}
