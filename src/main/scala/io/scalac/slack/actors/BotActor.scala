package io.scalac.slack.actors

import akka.actor.{Actor, ActorLogging, Props}
import io.scalac.slack.Config
import io.scalac.slack.actors.messages.{AuthTest, ApiTest, Start, Stop}
import io.scalac.slack.exceptions.{NotAuthenticated, SlackError}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created on 20.01.15 23:59
 */
class BotActor extends Actor with ActorLogging {

  import context.{dispatcher, system}

  override def receive: Receive = {
    case Start =>

      val api = context.actorOf(Props[ApiActor])

//      api ! ApiTest()

      api ! AuthTest(Config.apiKey)

//      system.scheduler.scheduleOnce(10 seconds, self, Stop)

    case Stop => system.shutdown()
    case ne : NotAuthenticated =>
      log.debug("Not authenticated")
    case se : SlackError =>
      log.error(se, "Error occured")
  }

}
