package io.scalac.slack.bots

import io.scalac.slack.common.{Command, OutboundMessage}

import scala.util.Random

/**
 * Maintainer: Mario
 */
class HelloBot extends AbstractBot {
  val welcomes = List("what's up?", "how's going?", "ready for work?", "nice to see you again")

  def welcome = Random.shuffle(welcomes).head

  override def act: Receive = {
    case Command("hello", _, message) =>
      publish(OutboundMessage(message.channel, s"hello <@${message.user}>, $welcome"))
  }

  override def help(channel: String): OutboundMessage = OutboundMessage(channel, s"When you feel lonely and unwanted *${name}* is something for you. \\n " +
      s"`hello` - to talk with the bot")
}
