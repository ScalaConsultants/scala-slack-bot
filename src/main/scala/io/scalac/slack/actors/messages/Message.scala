package io.scalac.slack.actors.messages

/**
 * Created on 20.01.15 23:59
 */
sealed trait Message

object Start extends Message

//starts the system
object Stop extends Message


case class ApiTest(param: Option[String] = None, error: Option[String] = None)

case class Ok(param: Option[String])