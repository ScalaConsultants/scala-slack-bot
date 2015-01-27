package io.scalac.slack.actors

import akka.actor.{Actor, ActorLogging, Props}
import io.scalac.slack.Config
import io.scalac.slack.actors.messages._
import io.scalac.slack.exceptions.{NotAuthenticated, SlackError}

import scala.language.postfixOps

/**
 * Created on 20.01.15 23:59
 */
class BotActor extends Actor with ActorLogging {

  import context.system

  override def receive: Receive = {
    case Start =>

      val api = context.actorOf(Props[ApiActor])

      api ! ApiTest(None, None)

      api ! AuthTest(Config.apiKey)

    //      system.scheduler.scheduleOnce(10 seconds, self, Stop)

    case Stop => system.shutdown()
    case NotAuthenticated =>
      log.debug("Not authenticated")
    case SlackError =>
      log.error("SlackError occured")
    case Ok(args) =>
      println("GOT OK")
  }

}
