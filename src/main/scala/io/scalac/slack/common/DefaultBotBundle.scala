package io.scalac.slack.common

import akka.actor.{ActorContext, ActorRef, Props}
import io.scalac.slack.bots.gifs.{GifsBot, GifsRepository}
import io.scalac.slack.bots.recruitment.{EmployeeRepository, RecruitmentBot}
import io.scalac.slack.bots.{RichMessageTestBot, ImportantMessageBot, LoggingBot}
import io.scalac.slack.bots.digest.{DigestRepository, DigestBot}
import io.scalac.slack.bots.feedback.{FeedbackRepository, FeedbackBot}
import io.scalac.slack.bots.hello.HelloBot
import io.scalac.slack.bots.ping.PingPongBot
import io.scalac.slack.bots.repl.{Repl, ReplBot}
import io.scalac.slack.bots.system.{HelpBot, CommandsRecognizerBot}
import io.scalac.slack.bots.tags.{TagsRepository, TagsBot}
import io.scalac.slack.bots.twitter.{TwitterRepository, TwitterMessenger, TwitterBot}
import io.scalac.slack.{BotModules, Config, MessageEventBus, SlackBot}

class DefaultBotBundle extends BotModules {

  override def registerModules(context: ActorContext, websocketClient: ActorRef) = {

    val bus: MessageEventBus = SlackBot.eventBus

    val loggingBot = context.actorOf(Props[LoggingBot], "loggingBot")
    val pingpongBot = context.actorOf(Props[PingPongBot], "pingpongBot")
    val digestBot = context.actorOf(Props(classOf[DigestBot], new DigestRepository(), bus), "digestBot")
    val commandProcessor = context.actorOf(Props[CommandsRecognizerBot], "commandProcessor")
    val helloBot = context.actorOf(Props(classOf[HelloBot], bus),  "helloBot")
    val replBot =  context.actorOf(Props(classOf[ReplBot], new Repl(Config.scalaLibraryPath), bus), "replBot")
    val twitterBot =  context.actorOf(
      Props(classOf[TwitterBot],
        Config.twitterGuardians,
        new TwitterMessenger(Config.consumerKey,
          Config.consumerKeySecret,
          Config.accessToken,
          Config.accessTokenSecret),
        new TwitterRepository(),
        bus),
      "twitterBot"
    )
    val tagBot = context.actorOf(Props(classOf[TagsBot], new TagsRepository(), bus), "tagBot")
    val feedbackBot = context.actorOf(Props(classOf[FeedbackBot], new FeedbackRepository(), bus), "feedbackBot")
    val helpBot = context.actorOf(Props(classOf[HelpBot], bus), "helpBot")
    val importantMessageBot = context.actorOf(Props(classOf[ImportantMessageBot], bus), "importantMessageBot")
    val richMessageBot = context.actorOf(Props[RichMessageTestBot], "richMessageBot")
    val gifBot = context.actorOf(Props(classOf[GifsBot], new GifsRepository()), "gifBot") //TODO: use external bus
    val recruitmentBot = context.actorOf(Props(classOf[RecruitmentBot], new EmployeeRepository(), bus), "recruitmentBot")
  }
}
