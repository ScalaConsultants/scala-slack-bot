package io.scalac.slack.bots.voting

import akka.actor.{Props, ActorSystem}
import akka.testkit.TestKit
import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.BotTest
import io.scalac.slack.bots.voting.VotingBot.Vote
import io.scalac.slack.common.{OutboundMessage, Command, BaseMessage}
import org.mockito.Mockito._
import org.mockito.Matchers._

import scala.concurrent.duration._

class VotingBotTest(_system: ActorSystem) extends TestKit(_system) with BotTest {
  def this() = this(ActorSystem("HelpBotTestSystem"))

  val testBus: MessageEventBus =  new MessageEventBus

  def botUnderTest(repo: VotingRepo) = Props(classOf[VotingBot], repo, testBus)

  val base = BaseMessage(text = "", channel = "channel", user = "John", ts = "", edited = false)

  "VotingBotTest" must {

    "open voting session" in {
      val repo = mock[VotingRepo]
      val sessionId = 102
      when(repo.createSession(any[String], any[Array[String]])).thenReturn(sessionId)
      val bot = botUnderTest(repo)
      val parts = List("q1", "q2?;", "a1;", "a2", "a2;", "a3")

      matrix(bot) { entry =>
        entry ! Command("vote-open", parts, base)
        val partsArray = Array("q1 q2?", " a1", " a2 a2", " a3")
        theProbe.expectMsg(1 second, OutboundMessage(base.channel, VotingBot.formatOpenMessage(sessionId, base.user, partsArray)))

        verify(repo).createSession(partsArray.head, partsArray.tail)
      }
    }

    "allow to vote" in {
      val repo = mock[VotingRepo]

      val sessionId = 403
      val answerId = 0
      val vote = Vote(base.user, answerId)
      when(repo.addVote(sessionId, vote)).thenReturn(VoteResult.Voted)
      val bot = botUnderTest(repo)

      matrix(bot) { entry =>
        entry ! Command("vote", List(sessionId.toString, answerId.toString), base)
        theProbe.expectMsg(1 second, OutboundMessage(base.channel, VotingBot.formatVoteMessage(sessionId, base.user, answerId)))

        verify(repo).addVote(sessionId, vote)
      }
    }

    "disallow voting on not existing answers" in {
      val repo = mock[VotingRepo]

      val sessionId = 670
      val answerId = 101
      val vote = Vote(base.user, answerId)
      when(repo.addVote(sessionId, vote)).thenReturn(VoteResult.NoAnswer)
      val bot = botUnderTest(repo)

      matrix(bot) { entry =>
        entry ! Command("vote", List(sessionId.toString, answerId.toString), base)
        theProbe.expectMsg(1 second, OutboundMessage(base.channel, VotingBot.formatNoAnswerMessage(sessionId, base.user, answerId)))

        verify(repo).addVote(sessionId, vote)
      }
    }

    "disallow voting in closed sessions" in {
      val repo = mock[VotingRepo]

      val sessionId = 808
      val answerId = 0
      val vote = Vote(base.user, answerId)
      when(repo.addVote(sessionId, vote)).thenReturn(VoteResult.SessionClosed)
      val bot = botUnderTest(repo)

      matrix(bot) { entry =>
        entry ! Command("vote", List(sessionId.toString, answerId.toString), base)
        theProbe.expectMsg(1 second, OutboundMessage(base.channel, VotingBot.formatSessionClosedMessage(sessionId, base.user)))

        verify(repo).addVote(sessionId, vote)
      }
    }

    "disallow voting when session is missing" in {
      val repo = mock[VotingRepo]

      val sessionId = 460
      val answerId = 0
      val vote = Vote(base.user, answerId)
      when(repo.addVote(sessionId, vote)).thenReturn(VoteResult.NoSession)
      val bot = botUnderTest(repo)

      matrix(bot) { entry =>
        entry ! Command("vote", List(sessionId.toString, answerId.toString), base)
        theProbe.expectMsg(1 second, OutboundMessage(base.channel, VotingBot.formatNoSessionMessage(sessionId, base.user)))

        verify(repo).addVote(sessionId, vote)
      }
    }
  }
}
