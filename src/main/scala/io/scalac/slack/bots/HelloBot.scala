package io.scalac.slack.bots

import io.scalac.slack.SlackBot
import io.scalac.slack.common.{BaseMessage, Command, OutboundMessage}

import scala.util.Random

/**
 * Created on 16.02.15 21:32
 */
class HelloBot extends IncomingMessageListener {
  val welcomes = List("what's up?", "how's going?" /*, "ready for work?"*/ , "nice to see you")

  def welcome = Random.shuffle(welcomes).head

  override def receive: Receive = {
    case Command("hello", _, message) =>
      publish(OutboundMessage(message.channel, s"hello <@${message.user}>,\\n $welcome"))

    case BaseMessage(text, channel, user, _, _) =>
      SlackBot.botInfo match {
        case Some(bi) if (text.startsWith("hi ") || text.startsWith("hello ")) && (text.contains(bi.id) || text.contains(bi.name)) && user != bi.id =>

          //multiline message example
          // new line sign `\n` should be double escaped, slash twice.
            publish(OutboundMessage(channel, s"""hello <@$user>,\\n $welcome"""))

        case None => //nothing to do!

      }

  }
}
