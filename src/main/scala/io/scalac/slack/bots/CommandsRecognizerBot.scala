package io.scalac.slack.bots

import io.scalac.slack.common.{BaseMessage, Command}

/**
 * Created on 13.02.15 23:36
 */
class CommandsRecognizerBot extends IncomingMessageListener {

  val commandChar = '$'

  override def receive: Receive = {
    case bm @ BaseMessage(text, channel, user, dateTime, edited) =>
      //COMMAND links list with bot's nam jack can be called:
      // jack link list
      // @jack link list
      // !link list

      if (text.trim.startsWith(commandChar.toString)) {
        val tokenized = text.trim.drop(1).split("\\s")
        publish(Command(tokenized.head, tokenized.tail.toList, bm))
      }

  }
}
