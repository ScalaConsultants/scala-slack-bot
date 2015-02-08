package io.scalac.slack.common

/**
 * Created on 08.02.15 01:02
 */
sealed trait Event

//event z pełną treścią wiadomości
case class UnspecifiedEvent(text: String) extends Event
