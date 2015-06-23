package io.scalac.slack.bots

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import io.scalac.slack.MessageEventBus
import io.scalac.slack.common.{Incoming, Outgoing}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}


trait BotTest extends ImplicitSender with WordSpecLike with BeforeAndAfterAll with MockitoSugar {
  self: TestKit =>

  val testBus: MessageEventBus
  val theProbe = TestProbe()

  def getEchoSubscriber = {
    system.actorOf(Props(new Actor {
      def receive = {
        case msg =>
          theProbe.ref ! msg
      }
    }))
  }

  def matrix(bot: Props)(f: (ActorRef) => Unit) = {
    val eventBus = testBus
    val echo = getEchoSubscriber
    val entry = system.actorOf(bot)
    eventBus.subscribe(echo, Incoming)
    eventBus.subscribe(echo, Outgoing)
    f(entry)
  }

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)
}
