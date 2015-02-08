package io.scalac.slack

import akka.actor.{Props, Actor, ActorLogging}
import akka.pattern._
import akka.util.Timeout
import io.scalac.slack.api._
import io.scalac.slack.common.Message
import io.scalac.slack.common._
import io.scalac.slack.websockets.{MessageListenerActor, WSActor, WebSocket}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created on 20.01.15 23:59
 */
class SlackBotActor extends Actor with ActorLogging {

  import context.system

  val api = context.actorOf(Props[ApiActor])

  var errors = 0

  val websocketClient = context.actorOf(Props[WSActor], "ws-actor")
  //register listener to listen to messages
  val messageProcessor = context.actorOf(Props(new MessageListenerActor(websocketClient)), "message-processor")
  context.system.eventStream.subscribe(messageProcessor, classOf[Message])


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

      implicit val timeout: Timeout = 5.seconds

      log.info(s"Connecting to host [$host] and resource [$resource]")

      val connect = websocketClient ? WebSocket.Connect(host, 443, resource, withSsl = true)
      val result = Await.result(connect, timeout.duration)
      log.info(result.toString)


      Thread.sleep(3500L)//gracefully wait for start system

      //      context.system.eventStream.publish(Ping)
      websocketClient ! WebSocket.Send( s"""{
                                          |    "id": ${MessageCounter.next},
                                          |    "type": "ping",
                                          |    "time": ${SlackDateTime.timeStamp}
                                          |}""".stripMargin)

      BotModules.registerModules(context, websocketClient)

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
