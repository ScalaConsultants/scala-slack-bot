package io.scalac.slack.bots.twitter

import io.scalac.slack.bots.IncomingMessageListener
import io.scalac.slack.common.{OutboundMessage, Command}
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

/**
 * Authors:
 *  Patryk Jażdżewski
 */
class TwitterBot(twitter: Twitter) extends IncomingMessageListener {

  log.debug(s"Starting $this")

  val peopleToInform = " @patryk @mat "

  def saveToDb(msg: String) = {
    //TODO: needs DB
  }

  def receive = {
    case Command("twitter-post", twitText, message) =>
      val msg = twitText.mkString(" ")
      log.debug(s"Got x= twitter-post $msg from Slack")
      twitter.post(msg)
      saveToDb(msg)
      publish(OutboundMessage(message.channel, s"$msg has been posted to Twitter! $peopleToInform"))
  }
}

class Twitter(
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
