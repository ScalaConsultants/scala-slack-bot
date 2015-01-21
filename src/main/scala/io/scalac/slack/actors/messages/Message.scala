package io.scalac.slack.actors.messages

/**
 * Created on 20.01.15 23:59
 */
sealed trait Message
object Start extends Message //starts the system
object Stop extends Message //message for shutdown system
object Authorize extends Message //request for auth token
