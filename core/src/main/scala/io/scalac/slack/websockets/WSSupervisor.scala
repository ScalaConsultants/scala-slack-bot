package io.scalac.slack.websockets

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{ActorKilledException, OneForOneStrategy, Props, Actor}
import akka.actor.Actor.Receive
import io.scalac.slack.MessageEventBus


class WSSupervisor(eventBus: MessageEventBus) extends Actor {

  val wsActor = context.system.actorOf(Props(classOf[WSActor], eventBus), "ws-actor")

  override def receive: Receive = {
    case msg =>
      println(s"WS Supervisor received a $msg passing to $wsActor")
      wsActor
  }

  override val supervisorStrategy =
    OneForOneStrategy(maxNrOfRetries = 10) {
      case _: ActorKilledException     =>
        println(s"Actor was killed - restarting")
        Restart
    }
}
