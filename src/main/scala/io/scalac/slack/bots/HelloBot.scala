package io.scalac.slack.bots

import io.scalac.slack.common.{Command, OutboundMessage}

import scala.util.Random

/**
 * Created on 16.02.15 21:32
 */
class HelloBot extends IncomingMessageListener {
  val welcomes = List("what's up?", "how's going?", "ready for work?", "nice to see you again")

  def welcome = Random.shuffle(welcomes).head

  override def receive: Receive = {
    case Command("hello", _, message) =>
      publish(OutboundMessage(message.channel, s"hello <@${message.user}>, $welcome"))
  }
}
