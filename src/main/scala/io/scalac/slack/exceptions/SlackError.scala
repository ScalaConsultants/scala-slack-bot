package io.scalac.slack.exceptions

/**
 * Created on 21.01.15 16:23
 */
sealed trait SlackError

object ApiTestError extends SlackError

object NotAuthenticated extends SlackError

//no authentication token provided
object InvalidAuth extends SlackError

//invalid auth token
object AccountInactive extends SlackError

//token is for deleted user or team
case class UnknownError(msg: String) extends SlackError


object SlackError {
  def apply(errorName: String) = {
    errorName match {
      case "not_authed" => NotAuthenticated
      case "invalid_auth" => InvalidAuth
      case "account_inactive" => AccountInactive
      case err => new UnknownError(err)
    }
  }
}