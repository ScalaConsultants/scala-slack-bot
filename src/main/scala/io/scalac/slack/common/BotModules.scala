package io.scalac.slack.common

import akka.actor.{ActorContext, ActorRef, Props}
import io.scalac.slack.{SlackBot, MessageEventBus, Config}
import io.scalac.slack.bots._
import io.scalac.slack.bots.digest.{DigestRepository, DigestBot}
import io.scalac.slack.bots.feedback.{FeedbackRepository, FeedbackBot}
import io.scalac.slack.bots.hello.HelloBot
import io.scalac.slack.bots.ping.PingPongBot
import io.scalac.slack.bots.repl.{Repl, ReplBot}
import io.scalac.slack.bots.system.{CommandsRecognizerBot, HelpBot}
import io.scalac.slack.bots.tags.{TagsRepository, TagsBot}
import io.scalac.slack.bots.twitter.{TwitterRepository, TwitterMessenger, TwitterBot}

object BotModules {

  def registerModules(context: ActorContext, websocketClient: ActorRef) = {

    implicit val bus: MessageEventBus = SlackBot.eventBus

    val loggingBot = context.actorOf(Props[LoggingBot], "loggingBot")
    val pingpongBot = context.actorOf(Props[PingPongBot], "pingpongBot")
    val digestBot = context.actorOf(Props(classOf[DigestBot], new DigestRepository()), "digestBot")
    val commandProcessor = context.actorOf(Props[CommandsRecognizerBot], "commandProcessor")
    val helloBot = context.actorOf(Props[HelloBot], "helloBot")
    val replBot =  context.actorOf(Props(classOf[ReplBot], new Repl(Config.scalaLibraryPath)), "replBot")
    val twitterBot =  context.actorOf(
      Props(classOf[TwitterBot],
        Config.twitterGuardians,
        new TwitterMessenger(Config.consumerKey,
          Config.consumerKeySecret,
          Config.accessToken,
          Config.accessTokenSecret),
        new TwitterRepository()),
      "twitterBot"
    )
    val tagBot = context.actorOf(Props(classOf[TagsBot], new TagsRepository()), "tagBot")
    val feedbackBot = context.actorOf(Props(classOf[FeedbackBot], new FeedbackRepository()), "feedbackBot")
    val helpBot = context.actorOf(Props[HelpBot], "helpBot")
    val importantMessagebot = context.actorOf(Props[ImportantMessageBot])
  }
}
