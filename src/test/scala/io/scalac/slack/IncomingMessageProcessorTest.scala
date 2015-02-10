package io.scalac.slack

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import io.scalac.slack.common.{Incoming, IncomingMessage, UndefinedMessage}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created on 10.02.15 18:27
 */
class IncomingMessageProcessorTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll {

  def this() = this(ActorSystem("IncomingMessageProcessorTestSystem"))

  def eB() = new MessageEventBus

  val theProbe = TestProbe()

  def getEchoSubscriber = {
    system.actorOf(Props(new Actor {
      def receive = {
        case im: IncomingMessage =>
          theProbe.ref ! im
      }
    }))
  }

  /**
   * Why this function is named matrix?
   * Because I can!
   * @param f code to execute
   * @return nothing at all
   */
  def matrix()(f: (ActorRef) => Unit) = {
    implicit val eventBus = eB()
    val echo = getEchoSubscriber
    val entry = system.actorOf(Props(new IncomingMessageProcessor))
    eventBus.subscribe(echo, Incoming)
    f(entry)
  }

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "IncommingMessageProcessor" must {
    "push to event bus undefined message" in {

      matrix() { entry =>
        entry ! "just string!"
        theProbe.expectMsg(1 second, UndefinedMessage("just string!"))
      }
    }
  }
}
