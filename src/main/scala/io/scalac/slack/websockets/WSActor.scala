package io.scalac.slack.websockets

import akka.actor.{Actor, Props}
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

  override def receive = connect orElse handshaking orElse closeLogic

  val eventProcessor = context.actorOf(Props[EventProcessingActor], "event-processor")

  private def connect(): Receive = {
    case WebSocket.Connect(host, port, resource, ssl) =>
      val headers = List(
        HttpHeaders.Host(host, port),
        HttpHeaders.Connection("Upgrade"),
        HttpHeaders.RawHeader("Upgrade", "websocket"),
        HttpHeaders.RawHeader("Sec-WebSocket-Version", "13"),
        HttpHeaders.RawHeader("Sec-WebSocket-Key", Config.websocketKey))
      request = HttpRequest(HttpMethods.GET, resource, headers)
      IO(UHttp)(context.system) ! Http.Connect(host, port, ssl)
      sender() ! "connected"
  }

  override def businessLogic = {
    case WebSocket.Release => close()
    case TextFrame(msg) => //message received

      // Each message without parsing is sent to eventprocessor
      // Because all messages from websockets should be read fast
      // If EventProcessor slow down with parsing
      // can be used dispatcher
      eventProcessor ! msg.utf8String
      println("[WS_ACTOR] Received MEssage: " + msg.utf8String)
    case WebSocket.Send(message) => //message to send

      println(s"SENT MESSAGE: $message ")
      send(message)
    case ignoreThis => // ignore
  }

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

}

