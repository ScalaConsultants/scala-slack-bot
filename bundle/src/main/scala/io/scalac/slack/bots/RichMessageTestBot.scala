package io.scalac.slack.bots

import io.scalac.slack.{SlackBot, MessageEventBus}
import io.scalac.slack.common._

/**
 * Created on 16.02.15 23:11
 */
class RichMessageTestBot extends IncomingMessageListener {
  log.debug(s"Starting $this")
  
  override def receive: Receive = {
    case Command("rich", params, m) =>
      publish(RichOutboundMessage(m.channel, List(
        Attachment(
          Title("Hello title, should be link to Scalac's page", Some("http://scalac.io")),
          Color("#FF3400"),
          PreText("this is PreText"),
          Text("Now I'm talking with color and blocks"),
          Field("Field 1", "fill entire row", short = false),
          Field("Field 2", "fill half of the row", short = true),
          Field("Field 3", "fill half od the row", short = true),
          Field("Field 4", "fill entire row")
        ),
        Attachment(Title("Good message"), Text("something like that")),
        Attachment(Color.warning, Field("Teraz field", "taka sytuacja"), ImageUrl("http://www.scalac.io/img/logo/scalac_logo2.png"))
      )
      )
      )

  }

  override val bus: MessageEventBus = SlackBot.eventBus
}
