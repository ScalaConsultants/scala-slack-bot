package io.scalac.slack.common

import akka.actor.{ActorContext, ActorRef, Props}
import io.scalac.slack.Config
import io.scalac.slack.bots.digest.{DigestRepository, DigestBot}
import io.scalac.slack.bots.feedback.{FeedbackRepository, FeedbackBot}
import io.scalac.slack.bots.repl.ReplBot
import io.scalac.slack.bots.twitter.{TwitterRepository, TwitterMessenger, TwitterBot}
import io.scalac.slack.bots._

object BotModules {

  def registerModules(context: ActorContext, websocketClient: ActorRef) = {
    val loggingBot = context.actorOf(Props[LoggingBot])
    val pingpongBot = context.actorOf(Props[PingPongBot])
    val digestBot = context.actorOf(Props(classOf[DigestBot], new DigestRepository()))
    val commandProcessor = context.actorOf(Props[CommandsRecognizerBot])
    val helloBot = context.actorOf(Props[DialogBot])
    val replBot =  context.actorOf(Props(classOf[ReplBot], Config.scalaLibraryPath))
    val twitterBot =  context.actorOf(
      Props(classOf[TwitterBot],
        new TwitterMessenger(Config.consumerKey,
          Config.consumerKeySecret,
          Config.accessToken,
          Config.accessTokenSecret),
        new TwitterRepository())
    )
    val tagBot = context.actorOf(Props(classOf[TagsBot], new TagsRepository()))
    val feedbackBot = context.actorOf(Props(classOf[FeedbackBot], new FeedbackRepository()))
  }
}
