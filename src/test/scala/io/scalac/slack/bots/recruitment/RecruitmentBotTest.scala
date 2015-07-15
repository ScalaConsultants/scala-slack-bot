package io.scalac.slack.bots.recruitment

import akka.actor.{Props, ActorSystem}
import akka.testkit.TestKit
import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.BotTest
import io.scalac.slack.common.{OutboundMessage, Command, BaseMessage}
import org.mockito.Mockito._
import scala.concurrent.duration._

class RecruitmentBotTest (_system: ActorSystem) extends TestKit(_system) with BotTest  {
  def this() = this(ActorSystem("RecruitmentBotTestSystem"))

  override val testBus: MessageEventBus = new MessageEventBus

  def botUnderTest(me: MatcherEngine) = Props(classOf[RecruitmentBot], me, testBus)

  val userName = "author"
  
  "RecruitmentBot" should {
    "match candidate to Scalac recruiter" in {
      val mocked = mock[MatcherEngine]
      val bot = botUnderTest(mocked)
      val task = TaskData(
        url = "candidate_url",
        level = Measurable.Junior,
        focus = Measurable.Backend)

      when(mocked.matchCandidate(task)).thenReturn(None)

      matrix(bot) { entry =>
        val base = BaseMessage(
          text = s"${task.url} Junior Backend",
          channel = "channel", user = userName, ts = "", edited = false)

        entry ! Command("match-candidate", base.text.split(" ").toList, base)

        theProbe.expectMsg(10 second, OutboundMessage(base.channel, s"Bot matched NO MATCH for task ${task.url}"))
        verify(mocked).matchCandidate(task)
      }
    }

    "return error on unidentified level" in {
      val mocked = mock[MatcherEngine]
      val bot = botUnderTest(mocked)

      matrix(bot) { entry =>
        val base = BaseMessage(
          text = s"task_url !!Junior!! Backend",
          channel = "channel", user = userName, ts = "", edited = false)

        entry ! Command("match-candidate", base.text.split(" ").toList, base)

        theProbe.expectMsg(10 second, OutboundMessage(base.channel, s"No level for !!Junior!!. Use one of junior/medior/senior"))
        verifyZeroInteractions(mocked)
      }
    }

    "return error on unidentified focus" in {
      val mocked = mock[MatcherEngine]
      val bot = botUnderTest(mocked)

      matrix(bot) { entry =>
        val base = BaseMessage(
          text = s"task_url Junior !!Backend!!",
          channel = "channel", user = userName, ts = "", edited = false)

        entry ! Command("match-candidate", base.text.split(" ").toList, base)

        theProbe.expectMsg(10 second, OutboundMessage(base.channel, s"No focus for !!Backend!!. Use one of backend/frontend/mobile"))
        verifyZeroInteractions(mocked)
      }
    }
  }
}
