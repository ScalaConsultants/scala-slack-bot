package io.scalac.slack.bots.twitter

import io.scalac.slack.bots.IncomingMessageListener
import io.scalac.slack.common.{OutboundMessage, Command}
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder


class TwitterBot(twitter: Twitter) extends IncomingMessageListener {

  log.debug(s"Starting $this")


  def receive = {
    case Command("twitter-post", twitText :: _, message) =>
      log.debug(s"Got x= twitter-post $twitText from Slack")
      twitter.post(twitText)
      publish(OutboundMessage(message.channel, s"Link $twitText stored for Digest!"))
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
