package io.scalac.slack.bots.hello

import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.{Command, OutboundMessage}
import io.scalac.slack.SlackBot
import io.scalac.slack.common.{BaseMessage, Command, OutboundMessage}

import scala.util.Random

/**
 * Maintainer: Mario
 */
class HelloBot extends AbstractBot {
  val welcomes = List("what's up?", "how's going?", "ready for work?", "nice to see you")

  def welcome = Random.shuffle(welcomes).head

  override def act: Receive = {
    case Command("hello", _, message) =>
      publish(OutboundMessage(message.channel, s"hello <@${message.user}>,\\n $welcome"))

    case BaseMessage(text, channel, user, _, _) =>
      SlackBot.botInfo match {
        case Some(bi) if text.matches("(?i)(^|\\s*)(hi|hello)($|(\\s+.*))") && (text.contains(bi.id) || text.contains(bi.name)) && user != bi.id =>

          //multiline message example
          // new line sign `\n` should be double escaped, slash twice.
          publish(OutboundMessage(channel, s"""hello <@$user>,\\n $welcome"""))

        case _ => //nothing to do!

      }

  }

  override def help(channel: String): OutboundMessage = OutboundMessage(channel, s"When you feel lonely and unwanted *${name}* is something for you. \\n " +
      s"`hello` - to talk with the bot")
}
