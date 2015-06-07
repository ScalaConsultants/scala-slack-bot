package io.scalac.slack.bots.twitter

import java.io.InputStream
import java.net.URL

import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.{AbstractRepository, OutboundMessage, Command}
import org.joda.time.{DateTimeZone, DateTime}
import twitter4j.{StatusUpdate, Status, TwitterFactory}
import twitter4j.conf.ConfigurationBuilder

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.JdbcBackend.Database.dynamicSession
import scala.util.Try

/**
 * Maintainer: Patryk
 */
class TwitterBot(
    peopleToInform: String,
    twitter: TwitterMessenger,
    repo: TwitterRepository,
    override val bus: MessageEventBus) extends AbstractBot {

  def saveToDb(msg: String, user: String) = repo.create(msg, user)

  def countAll() = repo.count()

  def act = {
    case Command("twitter-post", tweetText, message) =>
      val formattedTweet = formatMessage(tweetText)
      log.debug(s"Got x= twitter-post $formattedTweet from Slack")

      val resultMessage: String = validateTweet(formattedTweet).getOrElse( postToTwitter(formattedTweet, message.user) )
      publish(OutboundMessage(message.channel, resultMessage))

    case Command("twitter-post-with-image", imageUrl :: tweetText, message) =>
      val formattedTweet = formatMessage(tweetText)
      log.debug(s"Got x= twitter-post-with-image image is $imageUrl text $formattedTweet from Slack")

      val imageTry = twitter.getImageStream(imageUrl)
      val resultMessage: String = validateTweet(formattedTweet, Option(imageTry)).getOrElse( postToTwitter(formattedTweet, imageTry.get, message.user) )
      publish(OutboundMessage(message.channel, resultMessage))
  }

  private def formatMessage(params: List[String]): String = {
    params.mkString(" ").replaceAll("\\\\@", "@").replaceAll("\\\\#", "#")
  }

  def postToTwitter(formattedTweet: String, byUser: String): String = {
    val status = twitter.post(new StatusUpdate(formattedTweet))
    saveToDb(formattedTweet, byUser)
    buildTweetPostedMessage(formattedTweet, status)
  }

  def postToTwitter(formattedTweet: String, imageStream: InputStream, byUser: String): String = {
    val update = new StatusUpdate(formattedTweet)
    update.setMedia("Image", imageStream)
    val status = twitter.post(update)
    saveToDb(formattedTweet, byUser)
    buildTweetPostedMessage(formattedTweet, status)
  }

  //None if correct, Some(readable error message) if invalid
  private def validateTweet(formattedTweet: String, imageTry: Option[Try[InputStream]] = None): Option[String] = {
    val len = formattedTweet.length
    val topicNum = formattedTweet.count(_ == '#')
    val mentionNum = formattedTweet.count(_ == '@')

    if(len >= 140 || topicNum == 0 || mentionNum == 0 || imageTry.map(_.isFailure).getOrElse(false)){
      Some(s"Error During validation. " +
        s"The message is $len characters long (max 140). " +
        s"Contains $topicNum topics (#) and $mentionNum mentions (@). " +
        s"Image Link is correct? ${imageTry.map(_.isSuccess).getOrElse(true)}")
    } else None
  }

  protected def buildTweetPostedMessage(msg: String, status: Status): String = {
    s"'$msg' has been posted to Twitter! This is our ${countAll()} published Tweet $peopleToInform. The link is ${buildTwitterLink(status)}"
  }

  protected def buildTwitterLink(status: Status): String = {
    s"https://twitter.com/${status.getUser.getId}/status/${status.getId}"
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

  def post(update: StatusUpdate): Status = {
    twitter.updateStatus(update)
  }

  def getImageStream(imageUrl: String) = Try {
    val url = new URL(imageUrl)
    url.openStream()
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