package io.scalac.slack.actors

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.WebClient
import io.scalac.slack.actors.messages.{Start, Stop}
import io.scalac.slack.api.methods.Api

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
 * Created on 20.01.15 23:59
 */
class BotActor extends Actor with ActorLogging {

  import context.{dispatcher, system}

  override def receive: Receive = {
    case Start =>
      log.debug("System online")

      val webClient = new WebClient()

      val futureResponse = webClient.request(Api.test(None, Some("gleba")))

      futureResponse onComplete {
        case Success(response) => println(response)
        case Failure(error) => println("An error has occured: " + error.getMessage)
      }

      system.scheduler.scheduleOnce(10 seconds, self, Stop)

    case Stop => system.shutdown()
  }

}
