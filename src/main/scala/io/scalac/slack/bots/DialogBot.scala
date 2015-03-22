package io.scalac.slack.bots

import io.scalac.slack.SlackBot
import io.scalac.slack.common.{BaseMessage, Command, OutboundMessage}

import scala.util.Random

/**
 * Created on 16.02.15 21:32
 */
class DialogBot extends IncomingMessageListener {
  log.debug(s"Starting $this")

  val welcomes = List("what's up?", "how's going?", "ready for work?", "nice to see you")

  def welcome = Random.shuffle(welcomes).head

  override def receive: Receive = {
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

}
