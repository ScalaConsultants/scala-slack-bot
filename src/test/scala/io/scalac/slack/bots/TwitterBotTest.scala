package io.scalac.slack.bots

import java.net.URL

import akka.actor.{Props, ActorSystem}
import akka.testkit.TestKit
import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.twitter.{TwitterRepository, TwitterMessenger, TwitterBot}
import io.scalac.slack.common.{OutboundMessage, Command, BaseMessage}
import org.mockito.Mockito._
import twitter4j.{StatusUpdate, User, Status}
import scala.concurrent.duration._
import scala.util.Try

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
      val msgText = "twitter-msg @wwwwwwwwweeeeeeeeerrff #wwwwwwweeeeeeeerrrrrffff"
      val userName = "author"
      val count = 1

      matrix(bot) { entry =>
        when(repo.count()).thenReturn(count)
        val base = BaseMessage(text = msgText, channel = "channel", user = userName, ts = "", edited = false)

        val status = mock[Status]
        val user = mock[User]
        when(status.getId).thenReturn(600394753251868673L)
        when(status.getUser).thenReturn(user)
        when(user.getId).thenReturn(3073307699L)

        when(messenger.post(new StatusUpdate(msgText))).thenReturn(status)

        val expectedLink = "https://twitter.com/3073307699/status/600394753251868673"

        entry ! Command("twitter-post", List(msgText), base)

        theProbe.expectMsg(1 second, OutboundMessage(base.channel, s"'$msgText' has been posted to Twitter! This is our ${count} published Tweet $people. The link is $expectedLink"))
        verify(messenger).post(new StatusUpdate(msgText))
        verify(repo).create(msgText, userName)
      }
    }

    "post to Twitter with image" in {
      val messenger = mock[TwitterMessenger]
      val repo = mock[TwitterRepository]

      val bot = botUnderTest(messenger, repo)
      val msgText = "twitter-msg @wwwwwwwwweeeeeeeeerrff #wwwwwwweeeeeeeerrrrrffff"
      val userName = "author"
      val count = 1
      val imageFile = "http://media.cmgdigital.com/shared/lt/lt_cache/thumbnail/400/img/photos/2014/10/13/32/be/88184277.jpg"

      matrix(bot) { entry =>
        when(repo.count()).thenReturn(count)
        val base = BaseMessage(text = msgText, channel = "channel", user = userName, ts = "", edited = false)

        val status = mock[Status]
        val user = mock[User]
        when(status.getId).thenReturn(600394753251868674L)
        when(status.getUser).thenReturn(user)
        when(user.getId).thenReturn(3073307600L)

        val update = new StatusUpdate(msgText)
        val imageTry = Try { new URL(imageFile).openStream()}
        update.setMedia("Image", imageTry.get)

        when(messenger.getImageStream(imageFile)).thenReturn(imageTry)
        when(messenger.post(update)).thenReturn(status)

        val expectedLink = "https://twitter.com/3073307600/status/600394753251868674"

        entry ! Command("twitter-post-with-image", List(imageFile, msgText), base)

        theProbe.expectMsg(1 second, OutboundMessage(base.channel, s"'$msgText' has been posted to Twitter! This is our ${count} published Tweet $people. The link is $expectedLink"))
        verify(messenger).post(update)
        verify(repo).create(msgText, userName)
      }
    }

    "don't post too long tweets" in {
      val messenger = mock[TwitterMessenger]
      val repo = mock[TwitterRepository]

      val bot = botUnderTest(messenger, repo)
      val longText = "1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0"

      matrix(bot) { entry =>
        val base = BaseMessage(text = longText, channel = "channel", user = "", ts = "", edited = false)

        entry ! Command("twitter-post", List(longText), base)

        verifyZeroInteractions(messenger)
        verifyZeroInteractions(repo)
      }
    }

    "don't post without mentions or topics" in {
      val messenger = mock[TwitterMessenger]
      val repo = mock[TwitterRepository]

      val bot = botUnderTest(messenger, repo)
      val noText = ""

      matrix(bot) { entry =>
        val base = BaseMessage(text = noText, channel = "channel", user = "", ts = "", edited = false)

        entry ! Command("twitter-post", List(noText), base)

        verifyZeroInteractions(messenger)
        verifyZeroInteractions(repo)
      }
    }

    "don't post with broken image" in {
      val messenger = mock[TwitterMessenger]
      val repo = mock[TwitterRepository]

      val bot = botUnderTest(messenger, repo)
      val msgText = "twitter-msg @wwwwwwwwweeeeeeeeerrff #wwwwwwweeeeeeeerrrrrffff"
      val file = ""

      matrix(bot) { entry =>
        val base = BaseMessage(text = msgText, channel = "channel", user = "", ts = "", edited = false)

        when(messenger.getImageStream(file)).thenReturn(Try{ throw new Exception("Should fail") })

        entry ! Command("twitter-post-with-image", List(file, msgText), base)

        verifyZeroInteractions(messenger)
        verifyZeroInteractions(repo)
      }
    }

  }
}
