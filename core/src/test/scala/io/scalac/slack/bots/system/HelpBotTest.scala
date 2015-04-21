package io.scalac.slack.bots.system

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.testkit.{TestProbe, ImplicitSender, TestKit}
import io.scalac.slack.{BotTest, MessageEventBus, SlackBot}
import io.scalac.slack.common._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, Matchers}

import scala.concurrent.duration._

class HelpBotTest(_system: ActorSystem) extends TestKit(_system) with BotTest {
  def this() = this(ActorSystem("HelpBotTestSystem"))

  val testBus: MessageEventBus =  new MessageEventBus

  def botUnderTest() = Props(classOf[HelpBot], testBus)

  "HelpBotTest" must {

    val bot = botUnderTest()

    "send internal command messages to one bot" in {
      matrix(bot) { entry =>
        val base = BaseMessage(text = "", channel = "channel", user = "", ts = "", edited = false)
        entry ! Command("help", List("botName"), base)
        theProbe.expectMsg(1 second, HelpRequest(Some("botName"), base.channel))
      }
    }

    "send internal command messages to all actors" in {
      matrix(bot) { entry =>
        val base = BaseMessage(text = "", channel = "channel", user = "", ts = "", edited = false)
        entry ! Command("help", List(), base)
        theProbe.expectMsg(1 second, HelpRequest(None, base.channel))
      }
    }
  }
}
