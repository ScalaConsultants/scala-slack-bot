package io.scalac.slack.bots.voting

import akka.actor.{Props, ActorSystem}
import akka.testkit.TestKit
import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.BotTest
import io.scalac.slack.bots.system.HelpBot
import io.scalac.slack.common.{HelpRequest, Command, BaseMessage}

import scala.concurrent.duration._

class VotingBotTest(_system: ActorSystem) extends TestKit(_system) with BotTest {
  def this() = this(ActorSystem("HelpBotTestSystem"))

  val testBus: MessageEventBus =  new MessageEventBus

  def botUnderTest() = Props(classOf[HelpBot], testBus)

  val base = BaseMessage(text = "", channel = "channel", user = "", ts = "", edited = false)

  "VotingBotTest" must {

    val bot = botUnderTest()

    "open voting session" in {
      matrix(bot) { entry =>
        entry ! Command("help", List("botName"), base)
        theProbe.expectMsg(1 second, HelpRequest(Some("botName"), base.channel))
      }
    }
  }
}
