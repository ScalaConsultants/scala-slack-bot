package io.scalac.slack.bots.digest

import akka.actor.{ActorRef, ActorLogging, Actor}
import io.scalac.slack.websockets.WebSocket

class DigestBotActor(ws: ActorRef) extends Actor with ActorLogging {

  println(s"Starting $this")

  val responseStr =
    """
      |{
      |    "id": 1,
      |    "type": "message",
      |    "channel": "slackbot-test",
      |    "text": "Hello world"
      |}
    """.stripMargin

  def receive = {
    case x =>
      log.debug(s"Got x= $x from Slack")
      ws ! WebSocket.Send(responseStr)
  }
}
