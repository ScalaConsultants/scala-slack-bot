package io.scalac.slack.bots.repl

import akka.actor.{ActorRef, Actor, Props, ActorSystem}
import akka.testkit.{TestProbe, ImplicitSender, TestKit}
import io.scalac.slack.SlackBot
import io.scalac.slack.common._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, Matchers}
import scala.concurrent.duration._
import org.mockito.Mockito._
import org.mockito.Matchers._

class ReplTest (_system: ActorSystem) extends TestKit(_system) with ImplicitSender with Matchers with WordSpecLike with BeforeAndAfterAll with MockitoSugar {
  def this() = this(ActorSystem("ReplBotTestSystem"))

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
    implicit val eventBus = SlackBot.eventBus
    val echo = getEchoSubscriber
    val entry = system.actorOf(bot)
    eventBus.subscribe(echo, Incoming)
    eventBus.subscribe(echo, Outgoing)
    f(entry)
  }

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  def botUnderTest(r: Repl) = Props(classOf[ReplBot], r)

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
