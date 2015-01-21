package io.scalac.slack.actors

import akka.actor.{Actor, ActorLogging, Props}
import io.scalac.slack.actors.messages.{ApiTest, Start, Stop}

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

      api ! ApiTest()


      system.scheduler.scheduleOnce(10 seconds, self, Stop)

    case Stop => system.shutdown()
  }

}
