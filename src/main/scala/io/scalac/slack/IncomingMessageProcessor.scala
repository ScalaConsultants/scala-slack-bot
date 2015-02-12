package io.scalac.slack

import akka.actor.{Actor, ActorLogging}
import io.scalac.slack.common._
import spray.json._

/**
 * Created on 08.02.15 23:36
 * Incoming message processor should parse incoming string
 * and change into proper protocol
 */
class IncomingMessageProcessor(implicit eventBus: MessageEventBus) extends Actor with ActorLogging {

  import io.scalac.slack.common.MessageJsonProtocol._

  override def receive: Receive = {

    case s: String =>
      try {
        val mType = s.parseJson.convertTo[MessageType]
        val incomingMessage: IncomingMessage = mType.messageType match {
          case "hello" => Hello
          case "pong" => Pong
          case "message" => parseMessage(s.parseJson.asJsObject).getOrElse(UndefinedMessage(s))
          case _ =>
            UndefinedMessage(s)
        }
        eventBus.publish(incomingMessage)
      }
      catch {
        case e : JsonParser.ParsingException =>
        eventBus.publish(UndefinedMessage(s))
      }
    case ignored => //nothing special
  }

  //TODO: move it up in the hierarchy
  def parseMessage(jsonMessage: JsObject) = {
    jsonMessage.fields.get("text").map(t => DirectMessage(t.compactPrint.replaceAll("\"", "")))
  }
}
