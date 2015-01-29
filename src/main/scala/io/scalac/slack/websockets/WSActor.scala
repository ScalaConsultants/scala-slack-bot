package io.scalac.slack.websockets

import akka.actor.{ActorSystem, Actor}
import akka.io.IO
import io.scalac.slack.Config
import spray.can.Http
import spray.can.server.UHttp
import spray.can.websocket.WebSocketClientWorker
import spray.can.websocket.frame.{TextFrame, CloseFrame, StatusCode, Frame}
import spray.http.{HttpHeaders, HttpMethods, HttpRequest}

/**
 * Created on 28.01.15 19:45
 */
class WSActor extends Actor with WebSocketClientWorker {

  override def receive = connect orElse handshaking orElse closeLogic
  private def connect() : Receive = {
    case WebSocket.Connect(host, port, resource, ssl) =>
      val headers = List(
        HttpHeaders.Host(host, port),
        HttpHeaders.Connection("Upgrade"),
        HttpHeaders.RawHeader("Upgrade", "websocket"),
        HttpHeaders.RawHeader("Sec-WebSocket-Version", "13"),
        HttpHeaders.RawHeader("Sec-WebSocket-Key", Config.websocketKey))
      request = HttpRequest(HttpMethods.GET, resource, headers)
      IO(UHttp)(ActorSystem("websocketwor")) ! Http.Connect(host, port, ssl)
  }

  override def businessLogic = {
    case WebSocket.Release => close()
    case TextFrame(msg) =>

      println("RECEIVED MESSAGE: "+msg.utf8String)
    case WebSocket.Send(message) =>
      println("SENT MESSAGE: "+message)
      send(message)
    case ignoreThis => // ignore
  }

  def send(message : String) = connection ! TextFrame(message)
  def close() = connection ! CloseFrame(StatusCode.NormalClose)
  private var request : HttpRequest = null
  override def upgradeRequest = request

}
