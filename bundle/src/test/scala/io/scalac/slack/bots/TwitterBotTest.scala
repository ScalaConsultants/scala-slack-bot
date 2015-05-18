package io.scalac.slack.bots

import akka.actor.{Props, ActorSystem}
import akka.testkit.TestKit
import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.twitter.{TwitterRepository, TwitterMessenger, TwitterBot}
import io.scalac.slack.common.{OutboundMessage, Command, BaseMessage}
import org.mockito.Mockito._
import twitter4j.{User, Status}
import scala.concurrent.duration._

class TwitterBotTest (_system: ActorSystem) extends TestKit(_system) with BotTest {
  def this() = this(ActorSystem("TwitterBotTestSystem"))

  val people = "ppl"

  override val testBus: MessageEventBus = new MessageEventBus
  def botUnderTest(t: TwitterMessenger, r: TwitterRepository) = Props(classOf[TwitterBot], people, t, r, testBus)

  "TwitterBot" should {
    "post to Twitter" in {
      val messenger = mock[TwitterMessenger]
      val repo = mock[TwitterRepository]
      val bot = botUnderTest(messenger, repo)
      val msg = "twitter-msg"
      val userName = "author"
      val count = 1

      matrix(bot) { entry =>
        when(repo.count()).thenReturn(count)
        val base = BaseMessage(text = msg, channel = "channel", user = userName, ts = "", edited = false)

        val status = mock[Status]
        val user = mock[User]
        when(status.getId).thenReturn(600394753251868673L)
        when(status.getUser).thenReturn(user)
        when(user.getId).thenReturn(3073307699L)

        when(messenger.post(msg)).thenReturn(status)

        val expectedLink = "https://twitter.com/3073307699/status/600394753251868673"

        entry ! Command("twitter-post", List(msg), base)

        theProbe.expectMsg(1 second, OutboundMessage(base.channel, s"'$msg' has been posted to Twitter! This is our ${count} published Tweet $people. The link is $expectedLink"))
        verify(messenger).post(msg)
        verify(repo).create(msg, userName)
      }
    }
  }
}
