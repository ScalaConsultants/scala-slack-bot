package io.scalac.slack.common

import akka.actor.{ActorContext, ActorRef, Props}
import io.scalac.slack.Config
import io.scalac.slack.bots.digest.DigestBot
import io.scalac.slack.bots.repl.ReplBot
import io.scalac.slack.bots.twitter.{Twitter, TwitterBot}
import io.scalac.slack.bots._

object BotModules {

  def registerModules(context: ActorContext, websocketClient: ActorRef) = {
    val loggingBot = context.actorOf(Props[LoggingBot])
    val pingpongBot = context.actorOf(Props[PingPongBot])
    val digestBot = context.actorOf(Props[DigestBot])
    val commandProcessor = context.actorOf(Props[CommandsRecognizerBot])
    val helloBot = context.actorOf(Props[HelloBot])
    val replBot =  context.actorOf(Props(classOf[ReplBot], Config.scalaLibraryPath))
    val twitterBot =  context.actorOf(
      Props(classOf[TwitterBot],
      new Twitter(Config.consumerKey,
        Config.consumerKeySecret,
        Config.accessToken,
        Config.accessTokenSecret)))
    val tagBot = context.actorOf(Props(classOf[TagsBot], new TagsRepository()))
  }
}
