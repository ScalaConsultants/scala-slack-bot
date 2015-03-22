package io.scalac.slack.bots.system

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.testkit.{TestProbe, ImplicitSender, TestKit}
import io.scalac.slack.{IncomingMessageProcessor, MessageEventBus}
import io.scalac.slack.common.{Incoming, SlackDateTime, IncomingMessage}
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, Matchers}

import scala.concurrent.duration._

class HelpBotTest(_system: ActorSystem) extends TestKit(_system) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll {

  def this() = this(ActorSystem("HelpBotTestSystem"))

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

  //date helpers
  val baseTime = new DateTime(2015, 2, 15, 8, 23, 45, 0)
  val uniqueTS = SlackDateTime.uniqueTimeStamp(baseTime)

  def matrix()(f: (ActorRef) => Unit) = {
    implicit val eventBus = eB()
    val echo = getEchoSubscriber
    val entry = system.actorOf(Props(new HelpBot))
    eventBus.subscribe(echo, Incoming)
    f(entry)
  }

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "HelpBotTest" must {
    "send internal command messages" in {
      matrix() {entry =>
        entry ! s"""{
                   |  "type": "message",
                   |  "channel": "C2147483705",
                   |  "user": "U2147483697",
                   |  "text": "$$help",
                   |  "ts": "$uniqueTS}"
                   |}""".stripMargin
//        theProbe.expectMsg(1 second, Hello)
      }
    }
  }
}
