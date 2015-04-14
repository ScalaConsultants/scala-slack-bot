package io.scalac.slack.bots.twitter

import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.{AbstractBot, IncomingMessageListener}
import io.scalac.slack.common.{AbstractRepository, OutboundMessage, Command}
import org.joda.time.{DateTimeZone, DateTime}
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession

/**
 * Maintainer: Patryk
 */
class TwitterBot(peopleToInform: String, twitter: TwitterMessenger, repo: TwitterRepository)
     (implicit override val bus: MessageEventBus) extends AbstractBot {

  def saveToDb(msg: String, user: String) = repo.create(msg, user)

  def countAll() = repo.count()

  def act = {
    case Command("twitter-post", twitText, message) =>
      val msg = twitText.mkString(" ")
      log.debug(s"Got x= twitter-post $msg from Slack")
      twitter.post(msg)
      saveToDb(msg, message.user)
      publish(OutboundMessage(message.channel, s"$msg has been posted to Twitter! This is our ${countAll()} published Tweet $peopleToInform"))
  }

  override def help(channel: String): OutboundMessage = OutboundMessage(channel,
    s"*${name}* brings company Twitter account to the masses. \\n " +
      s"`twitter-post {message}` - posts the given message to Twitter from company account")
}

class TwitterMessenger(
  consumerKey: String,
  consumerKeySecret: String,
  accessToken: String,
  accessTokenSecret: String ) {

  private val cb = new ConfigurationBuilder()
  cb.setDebugEnabled(true)
      .setOAuthConsumerKey(consumerKey)
      .setOAuthConsumerSecret(consumerKeySecret)
      .setOAuthAccessToken(accessToken)
      .setOAuthAccessTokenSecret(accessTokenSecret)
  private val tf = new TwitterFactory(cb.build())
  private val twitter = tf.getInstance()

  def post(text: String): Boolean = {
    twitter.updateStatus(text)
    true
  }
}

class TwitterRepository() extends AbstractRepository {
  /// definitions
  override val bucket = "TwitterBot"

  private class PublishedTweet(tag: Tag) extends Table[(Long, String, String, Long)](tag, s"${bucket}_PublishedTweet") {
    def id = column[Long]("PublishedTweetId", O.PrimaryKey, O.AutoInc)
    def text = column[String]("Text")
    def author = column[String]("Author")
    def added = column[Long]("Added")
    def * = (id, text, author, added)
  }
  private val published = TableQuery[PublishedTweet]

  db.withDynSession {
    if(migrationNeeded())
      published.ddl.create
  }

  //public methods

  def create(msg: String, user: String) = {
    db.withDynSession{
      val when = new DateTime(DateTimeZone.UTC).getMillis
      published.insert((-1L, msg, user, when))
    }
  }
  def count() = db.withDynSession{
    published.list.length //TODO: not efficient
  }
}