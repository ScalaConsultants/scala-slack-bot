package io.scalac.slack.websockets

import akka.actor.{ActorRef, Actor, ActorSystem}
import akka.io.IO
import io.scalac.slack.Config
import spray.can.Http
import spray.can.server.UHttp
import spray.can.websocket.WebSocketClientWorker
import spray.can.websocket.frame.{CloseFrame, StatusCode, TextFrame}
import spray.http.{HttpHeaders, HttpMethods, HttpRequest}

/**
 * Created on 28.01.15 19:45
 */
class WSActor extends Actor with WebSocketClientWorker {

  import WebSocket._

  private var registeredBots = List[ActorRef]()

  override def receive = connect orElse handshaking orElse closeLogic

  private def connect(): Receive = {
    case Connect(host, port, resource, ssl) =>
      val headers = List(
        HttpHeaders.Host(host, port),
        HttpHeaders.Connection("Upgrade"),
        HttpHeaders.RawHeader("Upgrade", "websocket"),
        HttpHeaders.RawHeader("Sec-WebSocket-Version", "13"),
        HttpHeaders.RawHeader("Sec-WebSocket-Key", Config.websocketKey))
      request = HttpRequest(HttpMethods.GET, resource, headers)
      IO(UHttp)(ActorSystem("websocketwor")) ! Http.Connect(host, port, ssl)

    case RegisterModule(newActor) =>
      registeredBots = newActor :: registeredBots
  }

  override def businessLogic = {
    case WebSocket.Release => close()
    case TextFrame(msg) => //message received
      println("RECEIVED MESSAGE: " + msg.utf8String)
      publishToBots(msg.utf8String)
    case WebSocket.Send(message) => //message to send
      println("SENT MESSAGE: " + message)
      send(message)
    case ignoreThis => // ignore
  }

  private def publishToBots(msg: String) =  {
    val protocolMsg = convertToprotocol(msg)
    registeredBots.foreach(_ ! protocolMsg)
  }

  private def convertToprotocol(msg: String) = msg //TODO: convert to common format - case classes/objects

  def send(message: String) = connection ! TextFrame(message)

  def close() = connection ! CloseFrame(StatusCode.NormalClose)

  private var request: HttpRequest = null

  override def upgradeRequest = request

}

object WebSocket {

  sealed trait WebSocketMessage

  case class Connect(
    host: String,
    port: Int,
    resource: String,
    withSsl: Boolean = false) extends WebSocketMessage

  case class Send(msg: String) extends WebSocketMessage

  case object Release extends WebSocketMessage

  case class RegisterModule(actor: ActorRef)
}

