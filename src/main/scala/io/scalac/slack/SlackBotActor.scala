package io.scalac.slack

import akka.actor.{Actor, ActorLogging, Props}
import io.scalac.slack.api._
import io.scalac.slack.websockets.{WSActor, WebSocket}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created on 20.01.15 23:59
 */
class SlackBotActor extends Actor with ActorLogging {

  import context.system

  val api = context.actorOf(Props[ApiActor])

  var errors = 0

  val websocketClient = context.actorOf(Props[WSActor])

  override def receive: Receive = {
    case Start =>
      //test connection
      log.info("trying to connect to Slack's server...")
      api ! ApiTest()
    case Stop =>
      shutdown()
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

      websocketClient ! WebSocket.Connect(host , 443, resource, withSsl = true)
    case MigrationInProgress =>
      errors = 0
      restart()
    case se: SlackError =>
      log.error(s"SlackError occured [${se.toString}]")
      restart()
  }

  def restart(): Unit = {
    import context.dispatcher
    errors += 1
    if (errors < 10) {
      log.error(s"connection error [$errors], repeat for 10 seconds")
      system.scheduler.scheduleOnce(10.seconds, self, Start)
    } else shutdown()
  }

  def shutdown(): Unit = {
    context.system.shutdown()
  }
}
