package io.scalac.slack.common

/**
 * Created on 08.02.15 22:04
 */
sealed trait MessageEvent

/**
 * Incoming message types
 */
trait IncomingMessage extends MessageEvent

case object Pong extends IncomingMessage


case class DirectMessage(text: String) extends IncomingMessage
case class UndefinedMessage(body: String) extends IncomingMessage


/**
 * Outgoing message types
 */
trait OutgoingMessage extends MessageEvent

case object Ping extends OutgoingMessage


/**
 * Classifier for message event
 */
sealed trait MessageEventType

object Incoming extends MessageEventType

object Outgoing extends MessageEventType
