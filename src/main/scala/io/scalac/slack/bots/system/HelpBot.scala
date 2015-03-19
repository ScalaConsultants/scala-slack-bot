package io.scalac.slack.bots.system

import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.{HelpRequest, Command, OutboundMessage}

class HelpBot extends AbstractBot {
  override def act: Receive = {
    case Command("help", options, raw) =>
      publish(HelpRequest(options.headOption, raw.channel))
  }

  override def help(channel: String): OutboundMessage = OutboundMessage(channel, s"${name} is for helping Duh")
}
