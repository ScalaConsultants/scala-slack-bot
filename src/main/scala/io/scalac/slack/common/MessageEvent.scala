package io.scalac.slack.common

import org.joda.time.DateTime

/**
 * Created on 08.02.15 22:04
 */
sealed trait MessageEvent

/**
 * Incoming message types
 */
trait IncomingMessage extends MessageEvent

case object Pong extends IncomingMessage

case object Hello extends IncomingMessage


case class DirectMessage(text: String) extends IncomingMessage

//last in the incoming messages hierarchy
case class UndefinedMessage(body: String) extends IncomingMessage


/**
 * Outgoing message types
 */
trait OutgoingMessage extends MessageEvent {
  def toJson: String
}

case object Ping extends OutgoingMessage {
  override def toJson = s"""{"id": ${MessageCounter.next}, "type": "ping","time": ${SlackDateTime.timeStamp()}}"""
}

case class OutboundMessage(channel: String, text: String) extends OutgoingMessage {
  override def toJson =
    s"""{
      | "id": ${MessageCounter.next},
      | "type": "message",
      | "channel": "$channel",
      | "text": "$text"
      |}""".stripMargin
}


/**
 * Classifier for message event
 */
sealed trait MessageEventType

object Incoming extends MessageEventType

object Outgoing extends MessageEventType

/**
 * Message Type is unmarshalling helper
 * that show what kind of type is incomming message
 * it's needed because of their similiarity
 */
case class MessageType(messageType: String, subType: Option[String])
