package io.scalac.slack.websockets

import akka.actor.{ActorSystem, Props}

import scala.concurrent.Await

/**
 * Created on 28.01.15 22:03
 */
object WSActorTest {

  def main(args: Array[String]) {
    implicit lazy val system = ActorSystem("TestEchoServer")
    var wsmsg = ""
    val wse = system.actorOf(Props[WSActor])
    //websocket echo service
    wse ! WebSocket.Connect("echo.websocket.org", 443, "/echo", withSsl = true)

    Thread.sleep(3500L) // wait for all servers to be cleanly started
    println("sending message")
    val rock = "Rock it with WebSocket"
    wse ! WebSocket.Send(rock)
    Thread.sleep(2000L)
    wse ! WebSocket.Release
    system.shutdown()
    Thread.sleep(1000L)
  }

}
