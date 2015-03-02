package io.scalac.slack.bots

import io.scalac.slack.common.{Command, RichOutboundMessage}

/**
 * Created on 16.02.15 23:11
 */
class RichMessageTest extends IncomingMessageListener {
  override def receive: Receive = {
    case Command("rich", params, m) =>

      publish(RichOutboundMessage(m.channel, params.mkString(" ")))
  }
}
