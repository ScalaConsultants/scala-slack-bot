package io.scalac.slack.bots.repl

import akka.actor.{Props, ActorSystem}
import akka.testkit.TestKit
import io.scalac.slack.bots.BotTest
import io.scalac.slack.MessageEventBus
import io.scalac.slack.common._
import scala.concurrent.duration._
import org.mockito.Mockito._

class ReplBotTest (_system: ActorSystem) extends TestKit(_system) with BotTest {
  def this() = this(ActorSystem("ReplBotTestSystem"))

  override val testBus: MessageEventBus = new MessageEventBus

  def botUnderTest(r: Repl) = Props(classOf[ReplBot], r, testBus)

  "Repl" should {
    "allow to reset the repl" in {
      val repl = mock[Repl]
      val bot = botUnderTest(repl)

      matrix(bot) { entry =>
        val base = BaseMessage(text = "", channel = "channel", user = "", ts = "", edited = false)
        entry ! Command("repl-reset", List(), base)
        theProbe.expectMsg(1 second, OutboundMessage(base.channel, "Repl reset"))
        verify(repl).reset()
      }
    }

    "interprete commands" in {
      val repl = mock[Repl]
      val bot = botUnderTest(repl)
      val code = "code"
      val interpreted = "interpreted code"
      when(repl.run(code)).thenReturn(interpreted)

      matrix(bot) { entry =>
        val base = BaseMessage(text = code, channel = "channel", user = "", ts = "", edited = false)
        entry ! Command("repl", List(code), base)
        theProbe.expectMsg(1 second, OutboundMessage(base.channel, interpreted))
        verify(repl).run(code)
      }
    }
  }
}
