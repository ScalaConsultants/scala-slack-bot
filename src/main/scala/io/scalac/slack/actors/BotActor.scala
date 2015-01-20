package io.scalac.slack.actors

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.actors.messages.{Start, Stop}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
/**
 * Created on 20.01.15 23:59
 */
class BotActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case Start =>
      log.debug("System online")

      context.system.scheduler.scheduleOnce(10 seconds, self, Stop)
    case Stop => context.system.shutdown()
  }
}
