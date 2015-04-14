package io.scalac.slack.bots.system

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.testkit.{TestProbe, ImplicitSender, TestKit}
import io.scalac.slack.SlackBot
import io.scalac.slack.common._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, Matchers}

import scala.concurrent.duration._

class HelpBotTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll {

  def this() = this(ActorSystem("HelpBotTestSystem"))

  def eb() = SlackBot.eventBus

  val theProbe = TestProbe()

  def getEchoSubscriber = {
    system.actorOf(Props(new Actor {
      def receive = {
        case im: IncomingMessage =>
          theProbe.ref ! im
      }
    }))
  }

  def matrix()(f: (ActorRef) => Unit) = {
    implicit val eventBus = eb()
    val echo = getEchoSubscriber
    val entry = system.actorOf(Props(new HelpBot))
    eventBus.subscribe(echo, Incoming)
    eventBus.subscribe(echo, Outgoing)
    f(entry)
  }

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "HelpBotTest" must {
    "send internal command messages to one bot" in {
      matrix() { entry =>
        val base = BaseMessage(text = "", channel = "channel", user = "", ts = "", edited = false)
        entry ! Command("help", List("botName"), base)
        theProbe.expectMsg(1 second, HelpRequest(Some("botName"), base.channel))
      }
    }

    "send internal command messages to all actors" in {
      matrix() { entry =>
        val base = BaseMessage(text = "", channel = "channel", user = "", ts = "", edited = false)
        entry ! Command("help", List(), base)
        theProbe.expectMsg(1 second, HelpRequest(None, base.channel))
      }
    }
  }
}
