package io.scalac.slack.bots

import io.scalac.slack.common.{AbstractRepository, OutboundMessage, Command}

/**
 * Authors:
 *  Patryk Jażdżewski
 */
class TagBot extends IncomingMessageListener {

  log.debug(s"Starting $this")

  def receive = {
    case Command("tag", wordList, message) =>
      val fullMsg = wordList.mkString(" ")
      val tags = wordList.filter(x => x.startsWith("[") && x.endsWith("]")).map(_.toLowerCase())
      log.debug(s"Got x= tag $fullMsg from Slack with tags $tags")
      val num = tags.map(TagRepository.create(_, fullMsg)).count(_ == true)
      publish(OutboundMessage(message.channel, s"$fullMsg has been posted tagged with #$num $tags"))
  }
}

object TagRepository extends AbstractRepository("tags") {
  def create(tag: String, msg: String): Boolean = {

    true
  }
}