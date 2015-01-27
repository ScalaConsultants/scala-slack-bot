package io.scalac.slack.actors.messages

import io.scalac.slack.api.APIKey

/**
 * Created on 20.01.15 23:59
 * Messages sends between actors
 */
sealed trait Message

object Start extends Message

object Stop extends Message

//API CALLS
case class ApiTest(param: Option[String] = None, error: Option[String] = None) extends Message

case class AuthTest(token: APIKey) extends Message


//API RESPONSES
case class Ok(args: Option[Map[String, String]]) extends Message

case class AuthData(url: String, team: String, user: String, teamId: String, userId: String) extends Message

object AuthData {
  def apply(atr: AuthData) = atr

}
