package io.scalac.slack.bots.recruitment

import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.{Command, OutboundMessage}

import scala.util.Random
import com.typesafe.config.{Config, ConfigObject, ConfigFactory}

/**
 * Maintainer: Patryk
 */
class RecruitmentBot(repo: MatcherEngine, override val bus: MessageEventBus) extends AbstractBot {
  import Measurable._

  override def help(channel: String): OutboundMessage = OutboundMessage(channel,
    s"*${name}* is a tool to help find reviewers for candidates \\n" +
      s"`match-candidate {link to candidate} {level, one of junior/medior/senior} {area of focus, one of backend/frontend/mobile }` - " +
        s"find a match for a given candidates among Scalac")

  override def act: Receive = {
    case Command("match-candidate", link :: level :: focus :: _, message) =>
      log.debug(s"Received request to match $link with $level/$focus")

      val result = (levelToDouble(level), focusToDouble(focus)) match {
        case (None, _) =>
          OutboundMessage(message.channel, s"No Level for $level. Use one of junior/medior/senior")
        case (_, None) =>
          OutboundMessage(message.channel, s"No focus for $focus. Use one of backend/frontend/mobile")
        case (Some(levelD), Some(focusD)) =>
          val url = link.replace("<", "").replace(">", "")

          val task = TaskData(url, levelD, focusD)
          val matching = repo.findClosest(task)

          OutboundMessage(message.channel, s"Bot matched ${matching.map(_.name).getOrElse("NO MATCH")} for task $url")
      }

      publish(result)
  }
}