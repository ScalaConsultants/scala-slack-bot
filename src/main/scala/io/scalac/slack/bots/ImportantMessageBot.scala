package io.scalac.slack.bots

import io.scalac.slack.common._

/**
 * Created on 16.02.15 23:11
 */
class ImportantMessageBot extends IncomingMessageListener {

  log.debug(s"Starting $this")

  def setImportant(message: BaseMessage, params: List[String]) = {
    publish(RichOutboundMessage(message.channel, List(
      Attachment(Color.danger, Title("Attention please!!"), Text(params.mkString(" ")))
    )))
  }

  override def receive: Receive = {
    case Command("important", params, m) => setImportant(m, params)
    case Command("!", params, m) => setImportant(m, params)
  }
}
