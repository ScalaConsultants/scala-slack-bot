package io.scalac.slack

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, Props}
import akka.util.Timeout
import io.scalac.slack.api._
import io.scalac.slack.common._
import io.scalac.slack.websockets.WebSocket

import scala.concurrent.duration._

/**
 * Created on 20.01.15 23:59
 */
class SlackBotActor extends Actor with ActorLogging {

  import context.system
  import context.dispatcher

  val api = context.actorOf(Props[ApiActor])

  var errors = 0

  val websocketClient = SlackBot.websocketClient

  override def receive: Receive = {
    case Start =>
      //test connection
      log.info("trying to connect to Slack's server...")
      api ! ApiTest()
    case Stop =>
      SlackBot.shutdown()
    case Ok(_) =>
      log.info("connected successfully...")
      log.info("trying to auth")
      api ! AuthTest(Config.apiKey)
    case ad: AuthData =>
      log.info("authenticated successfully")
      log.info("request for websocket connection...")
      api ! RtmStart(Config.apiKey)
    case RtmData(url) =>
      log.info("fetched WSS URL")
      log.info(url)
      log.info("trying to connect to websockets channel")
      val dropProtocol = url.drop(6)
      val host = dropProtocol.split('/')(0)
      val resource = dropProtocol.drop(host.length)

      implicit val timeout: Timeout = 5.seconds

      log.info(s"Connecting to host [$host] and resource [$resource]")

      websocketClient ! WebSocket.Connect(host, 443, resource, withSsl = true)

      context.system.scheduler.scheduleOnce(Duration.create(5, TimeUnit.SECONDS), self, RegisterModules)

    case RegisterModules =>
      BotModules.registerModules(context, websocketClient)

    case MigrationInProgress =>
      log.warning("MIGRATION IN PROGRESS, next try for 10 seconds")
      system.scheduler.scheduleOnce(10.seconds, self, Start)
    case se: SlackError if errors < 10 =>
      errors += 1
      log.error(s"connection error [$errors], repeat for 10 seconds")
      log.error(s"SlackError occured [${se.toString}]")
      system.scheduler.scheduleOnce(10.seconds, self, Start)
    case se: SlackError =>
      log.error(s"SlackError occured [${se.toString}]")
      SlackBot.shutdown()
  }

}
